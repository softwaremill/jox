package com.softwaremill.jox.flows;

import com.softwaremill.jox.*;
import com.softwaremill.jox.structured.Scopes;
import com.softwaremill.jox.structured.ThrowingFunction;
import com.softwaremill.jox.structured.UnsupervisedScope;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.softwaremill.jox.Select.selectOrClosed;

class GroupByImpl<T, V, U> {

    private class FromParent {
        private final T v;

        public FromParent(T v) {
            this.v = v;
        }
    }

    private class PendingFromParent {
        private final T t;
        private final V v;
        private final long counter;

        public PendingFromParent(T t, V v, long counter) {
            this.t = t;
            this.v = v;
            this.counter = counter;
        }
    }

    private class ChildDone {
        private final V v;

        public ChildDone(V v) {
            this.v = v;
        }
    }

    private final Flow<T> parent;
    private final int parallelism;
    private final ThrowingFunction<T, V> predicate;
    private final Flow.ChildFlowTransformer<T, V, U> childFlowTransform;

    public GroupByImpl(Flow<T> parent, int parallelism, ThrowingFunction<T, V> predicate, Flow.ChildFlowTransformer<T, V, U> childFlowTransform) {
        this.parent = parent;
        this.parallelism = parallelism;
        this.predicate = predicate;
        this.childFlowTransform = childFlowTransform;
    }

    // Group by's (immutable) state, which is updated in the main `while`+`select` loop below.
    private class GroupByState {
        private final boolean parentDone;
        private final Optional<PendingFromParent> pendingFromParent;
        private final Map<V, Channel<T>> children;
        // Counter of elements received from the parent.
        private final long fromParentCounter;
        // A heap with group value (`V`) elements, weighted by the last parent element, mapped to that value.
        // Used to complete child flows, which haven't received an element the longest.
        private final WeightedHeap<V> childMostRecentCounters;

        private GroupByState() {
            this.parentDone = false;
            this.pendingFromParent = Optional.empty();
            this.children = new HashMap<>();
            this.fromParentCounter = 0L;
            this.childMostRecentCounters = new WeightedHeap<>();
        }

        private GroupByState(boolean parentDone, Optional<PendingFromParent> pendingFromParent,
                             Map<V, Channel<T>> children, long fromParentCounter,
                             WeightedHeap<V> childMostRecentCounters) {
            this.parentDone = parentDone;
            this.pendingFromParent = pendingFromParent;
            this.children = children;
            this.fromParentCounter = fromParentCounter;
            this.childMostRecentCounters = childMostRecentCounters;
        }

        public GroupByState withChildAdded(V v, Channel<T> childChannel) {
            Map<V, Channel<T>> newChildren = new HashMap<>(this.children);
            newChildren.put(v, childChannel);
            return new GroupByState(this.parentDone, this.pendingFromParent, newChildren, this.fromParentCounter, this.childMostRecentCounters);
        }

        public GroupByState withChildRemoved(V v) {
            Map<V, Channel<T>> newChildren = new HashMap<>(this.children);
            newChildren.remove(v);
            return new GroupByState(this.parentDone, this.pendingFromParent, newChildren, this.fromParentCounter, this.childMostRecentCounters);
        }

        public GroupByState withPendingFromParent(T t, V v, long counter) {
            return new GroupByState(this.parentDone, Optional.of(new PendingFromParent(t, v, counter)), this.children, this.fromParentCounter, this.childMostRecentCounters);
        }

        public GroupByState withPendingFromParent(Optional<PendingFromParent> pendingFromParent) {
            return new GroupByState(this.parentDone, pendingFromParent, this.children, this.fromParentCounter, this.childMostRecentCounters);
        }

        public GroupByState withParentDone(boolean done) {
            return new GroupByState(done, this.pendingFromParent, this.children, this.fromParentCounter, this.childMostRecentCounters);
        }

        public GroupByState withFromParentCounterIncremented() {
            return new GroupByState(this.parentDone, this.pendingFromParent, this.children, this.fromParentCounter + 1, this.childMostRecentCounters);
        }

        public GroupByState withChildCounter(V v, long counter) {
            WeightedHeap<V> newHeap = this.childMostRecentCounters.insert(v, counter);
            return new GroupByState(this.parentDone, this.pendingFromParent, this.children, this.fromParentCounter, newHeap);
        }

        public Map.Entry<Optional<V>, GroupByState> withoutLongestInactiveChild() {
            Map.Entry<Optional<WeightedHeap.HeapNode<V>>, WeightedHeap<V>> result = this.childMostRecentCounters.extractMin();
            WeightedHeap<V> newHeap = result.getValue();
            return Map.entry(result.getKey().map(WeightedHeap.HeapNode::item), new GroupByState(this.parentDone, this.pendingFromParent, this.children, this.fromParentCounter, newHeap));
        }

        public boolean elementsCanBeReceived() {
            return !(this.children.isEmpty() && this.parentDone);
        }

        public boolean shouldReceiveFromParentChannel() {
            return this.parentDone || this.pendingFromParent.isPresent();
        }
    }

    public Flow<U> run() {
        return Flows.usingEmit(emit -> {
            Scopes.unsupervised(scope -> {
                // Channel where all elements emitted by child flows will be sent; we use such a collective channel instead of
                // enumerating all child channels in the main `select`, as `select`s don't scale well with the number of
                // clauses. The elements from this channel are then emitted by the returned flow.
                Channel<U> childOutput = Flow.newChannelWithBufferSizeFromScope();

                // Channel where completion of children is signalled (because the parent is complete, or the parallelism limit
                // is reached).
                Channel<ChildDone> childDone = Channel.newUnlimitedChannel();

                // Parent channel, from which we receive as long as it's not done, and only when a child flow isn't pending
                // creation (see below). As the receive is conditional, the errors that occur on this channel are also
                // propagated to `childOutput`, which is always the first (priority) clause in the main `select`.
                Source<FromParent> parentChannel = parent.map(FromParent::new)
                        .onError(childOutput::errorOrClosed)
                        .runToChannel(scope);

                GroupByState state = new GroupByState();

                // Main loop; while there are any values to receive (from parent or children).
                while (state.elementsCanBeReceived()) {
                    assert state.children.size() <= parallelism;

                    // We do not receive from the parent when it's done, or when there's already a pending child flow to create
                    // (but can't be created because of `parallelism` limit); we always receive from child output & child done
                    // signals. In case of parent's error, the error is also propagated above to `childOutput`, so that it's
                    // quickly received. Receiving from child output has priority over child done signals, to receive all child
                    // values before marking a child as done.
                    List<Source<?>> pool = state.shouldReceiveFromParentChannel()
                            ? List.of(childOutput, childDone)
                            : List.of(childOutput, childDone, parentChannel);
                    var selectClauses = pool.stream().map(Source::receiveClause).toArray(SelectClause[]::new);

                    var result = selectOrClosed(selectClauses);
                    switch (result) {
                        case ChannelDone _ -> {
                            // Only the parent can be done; child completion is signalled via a value in `childDone`.
                            state = state.withParentDone(parentChannel.isClosedForReceive());
                            assert state.parentDone;
                            state = doCompleteAll(state);
                        }
                        case ChannelError channelError -> throw channelError.toException();
                        case Object o -> {
                            // for some reason compiler shows error when using instanceof / switch pattern matching
                            if (FromParent.class.isInstance(o)) {
                                //noinspection unchecked
                                FromParent fromParent = (FromParent) o;
                                state = state.withFromParentCounterIncremented();
                                state = sendToChildOrRunChildOrBuffer(state, childDone, childOutput, fromParent.v, predicate.apply(fromParent.v), state.fromParentCounter, scope);
                            } else if (ChildDone.class.isInstance(o)) {
                                //noinspection unchecked
                                ChildDone childDoneResult = (ChildDone) o;
                                state = state.withChildRemoved(childDoneResult.v);
                                // Children should only be done because their `childChannel` was completed as done by
                                // `sendToChild_orRunChild_orBuffer`, then `childMostRecentCounters` should have `v` removed.
                                // If it's still present, this indicates that the child flow was completed as done while the source
                                // child channel is not, which is invalid usage.
                                if (state.childMostRecentCounters.contains(childDoneResult.v)) {
                                    throw new IllegalStateException("Invalid usage of child flows: child flow was completed as done by user code (in childFlowTransform), while this is not allowed (see documentation for details)");
                                }

                                state = runChildIfPending(state, childDone, childOutput, scope);
                            } else {
                                //noinspection unchecked
                                emit.apply((U) o); // forwarding from `childOutput`
                            }
                        }
                    }
                }
                return null;
            });
        });
    }

    // Running a pending child flow, after another has completed as done
    private GroupByState runChildIfPending(GroupByState state, Channel<ChildDone> childDone, Channel<U> childOutput, UnsupervisedScope scope) throws InterruptedException {
        var s = state;
        if (s.pendingFromParent.isPresent()) {
            PendingFromParent pending = s.pendingFromParent.get();
            s = sendToChildOrRunChildOrBuffer(s.withPendingFromParent(Optional.empty()), childDone, childOutput, pending.t, pending.v, pending.counter, scope);
        }
        return s;
    }

    private GroupByState sendToChildOrRunChildOrBuffer(GroupByState state, Channel<ChildDone> childDone, Channel<U> childOutput, T t, V v, long counter,
                                                       UnsupervisedScope scope) throws InterruptedException {
        var s = state.withChildCounter(v, counter);

        if (s.children.containsKey(v)) {
            s.children.get(v).send(t);
        } else if (s.children.size() < parallelism) {
            // Starting a new child flow, running in the background; the child flow receives values via a channel,
            // and feeds its output to `childOutput`. Done signals are forwarded to `childDone`; elements & errors
            // are propagated to `childOutput`.
            Channel<T> childChannel = Flow.newChannelWithBufferSizeFromScope();
            s = s.withChildAdded(v, childChannel);

            scope.forkUnsupervised(() -> {
                childFlowTransform.apply(v).apply(Flows.fromSource(childChannel))
                        .onDone(() -> {
                            try {
                                childDone.sendOrClosed(new ChildDone(v));
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        // When the child flow is done, making sure that the source channel becomes closed as well
                        // otherwise, we'd be risking a deadlock, if there are `childChannel.send`-s pending, and the
                        // buffer is full; if the channel is already closed, this is a no-op.
                        .onDone(childChannel::doneOrClosed)
                        .onError(childChannel::errorOrClosed)
                        .runPipeToSink(childOutput, false);
                return null;
            });

            childChannel.send(t);
        } else {
            assert s.pendingFromParent.isEmpty();
            s = s.withPendingFromParent(t, v, counter);

            // Completing as done the child flow which didn't receive an element for the longest time. After
            // the flow completes, it will send `ChildDone` to `childDone`.
            Map.Entry<Optional<V>, GroupByState> result = s.withoutLongestInactiveChild();
            if (result.getKey().isPresent()) {
                s.children.get(result.getKey().get()).done();
            }
            s = result.getValue();
        }

        return s;
    }

    private GroupByState doCompleteAll(GroupByState state) {
        var s = state;
        while (true) {
            Map.Entry<Optional<V>, GroupByState> result = s.withoutLongestInactiveChild();
            if (result.getKey().isPresent()) {
                s.children.get(result.getKey().get()).done();
                s = result.getValue();
            } else {
                return result.getValue();
            }
        }
    }

}