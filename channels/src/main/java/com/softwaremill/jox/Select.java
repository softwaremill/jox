package com.softwaremill.jox;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

public class Select {
    /*
     Inspired by Kotlin's implementation: https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/common/src/selects/Select.kt

     Each select invocation proceeds through a couple of states. The state is stored in an internal representation
     of the select, a `SelectInstance`.

     First, the select starts in the `REGISTERING` state. For each clause, we call its `register` method, which in turn
     reserves a send/receive cell in the channel, and stores a `StoredSelect` instance there. This instance, apart from
     the `SelectInstance`, also holds the segment & index of the cell, so that we can clean up later, when another
     clause is selected.

     During registration, if another thread tries to select a clause concurrently, it's prevented from doing so;
     instead, we collect the clauses for which this happened in a list, and re-register them. Such a thread treats
     such invocations as if the cell was interrupted, and retries with a new cell. Later, we clean up the stored
     selects, which are re-registered, so that there are no memory leaks.

     Regardless of the outcome, clean up is always called at some point for each `StoredSelect`, apart from the one
     corresponding to the selected clause. The cleanup sets the cell's state to an interrupted sender or receiver, and
     updates the segment's counter appropriately.

     It's possible that a clause is completed immediately during registration. If that's the case, we overwrite the
     state (including a potentially concurrently set closed state), and cease further registrations.

     Any of the states set during registration are acted upon in `checkStateAndWait`. The method properly cleans up in
     case a clause was selected, or a channel becomes closed. If it sees a `REGISTERING` state, the state is changed
     to the current `Thread`, and the computation is suspended.

     On the other hand, other threads which encounter a `StoredSelect` instance in a channel's cell, call the
     `SelectInstance`'s methods: either `trySelect` or `channelClosed`. These change the state appropriately, optionally
     waking up the suspended thread to let it know, that it should inspect the state again. If the state change is
     successful, the cell's state is updated. Otherwise, it's the responsibility of the cleanup procedure to update it.
     */

    /**
     * Select exactly one clause to complete. Each clause should be created for a different channel.
     * <p>
     * If a couple of the clauses can be completed immediately, the select is biased towards the clauses that appear
     * first.
     * <p>
     * If no clauses are given, throws {@link ChannelDoneException}.
     *
     * @param clauses The clauses, from which one will be selected. Array must not be empty or {@code null} and
     *                can't contain {@code null} values.
     * @return The value returned by the selected clause.
     * @throws ChannelClosedException When any of the channels is closed (done or in error).
     */
    @SafeVarargs
    public static <U> U select(SelectClause<? extends U>... clauses) throws InterruptedException {
        var r = selectOrClosed(clauses);
        if (r instanceof ChannelClosed c) {
            throw c.toException();
        } else {
            //noinspection unchecked
            return (U) r;
        }
    }

    /**
     * Select exactly one clause to complete. Each clause should be created for a different channel.
     * Doesn't throw exceptions when the channel is closed, but returns a value.
     * <p>
     * If a couple of the clauses can be completed immediately, the select is biased towards the clauses that appear
     * first.
     * <p>
     * If no clauses are given, returns {@link ChannelDone}.
     *
     * @param clauses The clauses, from which one will be selected. Array must not be empty or {@code null} and
     *                can't contain {@code null} values.
     * @return Either the value returned by the selected clause, or {@link ChannelClosed}, when any of the channels
     * is closed (done or in error).
     */
    @SafeVarargs
    public static <U> Object selectOrClosed(SelectClause<? extends U>... clauses) throws InterruptedException {
        while (true) {
            if (clauses == null || clauses.length == 0) {
                // no clauses given
                throw new IllegalArgumentException("No clauses given");
            }
            if (Arrays.stream(clauses).anyMatch(Objects::isNull)) {
                // null clauses given
                throw new IllegalArgumentException("Null clauses are not supported");
            }

            var r = doSelectOrClosed(clauses);
            //noinspection StatementWithEmptyBody
            if (r == RestartSelectMarker.RESTART) {
                // in case a `CollectSource` function filters out the element (the transformation function returns `null`,
                // which is represented as a marker because `null` is a valid result of `doSelectorClosed`, e.g. for send clauses),
                // we need to restart the selection process

                // next loop
            } else {
                return r;
            }
        }
    }

    /**
     * Select exactly one clause to complete, with a timeout. Each clause should be
     * created for a different channel.
     * <p>
     * If a couple of the clauses can be completed immediately, the select is biased
     * towards the clauses that appear first.
     * <p>
     * If no clauses are given, throws {@link IllegalArgumentException}.
     * <p>
     * If the timeout elapses before any clause can be selected, throws
     * {@link TimeoutException}.
     *
     * @param timeout The maximum time to wait for a clause to be selected.
     * @param clauses The clauses, from which one will be selected. Array must not
     *                be empty or {@code null} and can't contain {@code null}
     *                values.
     * @return The value returned by the selected clause.
     * @throws TimeoutException     When the timeout elapses before any clause can
     *                              be selected.
     * @throws InterruptedException When the current thread is interrupted.
     */
    @SafeVarargs
    public static <U> U selectWithin(Duration timeout, SelectClause<? extends U>... clauses)
            throws InterruptedException, TimeoutException {
        var timeoutValue = TimeoutMarker.INSTANCE;
        var result = selectOrClosedWithin(timeout, timeoutValue, clauses);
        if (result == timeoutValue) {
            throw new TimeoutException("Select timed out after " + timeout.toMillis() + " ms");
        } else if (result instanceof ChannelClosed c) {
            throw c.toException();
        } else {
            // noinspection unchecked
            return (U) result;
        }
    }

    /**
     * Select exactly one clause to complete, with a timeout. Each clause should be
     * created for a different channel.
     * Doesn't throw exceptions when the channel is closed or timeout occurs, but
     * returns a value.
     * <p>
     * If a couple of the clauses can be completed immediately, the select is biased
     * towards the clauses that appear first.
     * <p>
     * If no clauses are given, returns the timeout value.
     * <p>
     * If the timeout elapses before any clause can be completed, returns the
     * timeout value.
     *
     * @param timeout      The maximum time to wait for a clause to be selected.
     * @param timeoutValue The value to return if the timeout elapses.
     * @param clauses      The clauses, from which one will be selected. Array must
     *                     not be empty or {@code null} and
     *                     can't contain {@code null} values.
     * @return Either the value returned by the selected clause, the timeout value
     * when timeout occurs, or
     * {@link ChannelClosed} when any of the channels is closed (done or in
     * error).
     * @throws InterruptedException When the current thread is interrupted.
     */
    @SafeVarargs
    public static <U> Object selectOrClosedWithin(Duration timeout, U timeoutValue,
                                                  SelectClause<? extends U>... clauses) throws InterruptedException {
        if (timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("Timeout must be positive");
        }

        // Create a timeout channel and virtual thread to send the timeout signal
        Channel<TimeoutMarker> timeoutChannel = Channel.newBufferedChannel(1);
        var timeoutThread = Thread.ofVirtual().start(() -> {
            try {
                Thread.sleep(timeout.toMillis());
                timeoutChannel.sendOrClosed(TimeoutMarker.INSTANCE);
            } catch (InterruptedException e) {
                // this might happen - ignore, since we don't want the uncaught exception handler to be called
            }
        });

        // Create a new array with the timeout clause appended
        @SuppressWarnings("unchecked")
        SelectClause<Object>[] clausesWithTimeout = Arrays.copyOf(clauses, clauses.length + 1, SelectClause[].class);
        clausesWithTimeout[clauses.length] = timeoutChannel.receiveClause(marker -> timeoutValue);

        try {
            return selectOrClosed(clausesWithTimeout);
        } finally {
            // Always clean up the timeout thread
            timeoutThread.interrupt();
            joinUninterruptibly(timeoutThread);
        }
    }

    private static void joinUninterruptibly(Thread thread) throws InterruptedException {
        InterruptedException intercepted = null;
        while (true) {
            try {
                thread.join();
                break;
            } catch (InterruptedException e) {
                if (intercepted == null) intercepted = e;
                else intercepted.addSuppressed(e);
            }
        }

        if (intercepted != null) {
            throw intercepted;
        }
    }

    @SafeVarargs
    private static <U> Object doSelectOrClosed(SelectClause<? extends U>... clauses) throws InterruptedException {
        // short-circuiting if any of the channels is in error; otherwise, we might have selected a clause, for which
        // a value was available immediately - even though a channel for a clause appearing later was in error
        var anyError = getAnyChannelInError(clauses);
        if (anyError != null) {
            return anyError;
        }

        // check that the clause doesn't refer to a channel that is already used in a different clause
        var allRendezvous = verifyChannelsUnique_getAreAllRendezvous(clauses);

        var si = new SelectInstance(clauses.length);
        for (int i = 0; i < clauses.length; i++) {
            SelectClause<?> clause = clauses[i];
            if (clause instanceof DefaultClause<?> && i != clauses.length - 1) {
                throw new IllegalArgumentException("The default clause can only be the last one.");
            }
            if (!si.register(clause)) {
                break; // channel is closed, or a clause was selected - in both cases, no point in further registrations
            }
        }

        return si.checkStateAndWait(allRendezvous);
    }

    private static boolean verifyChannelsUnique_getAreAllRendezvous(SelectClause<?>[] clauses) {
        var allRendezvous = true;
        // we expect the number of clauses to be small, so that this n^2 double-loop is faster than allocating a set
        for (int i = 0; i < clauses.length; i++) {
            var chi = clauses[i].getChannel();
            for (int j = i + 1; j < clauses.length; j++) {
                if (chi == clauses[j].getChannel()) {
                    throw new IllegalArgumentException("Channel " + chi + " is used in multiple clauses");
                }
            }
            allRendezvous = allRendezvous && (chi == null || chi.isRendezvous);
        }
        return allRendezvous;
    }

    private static ChannelError getAnyChannelInError(SelectClause<?>[] clauses) {
        for (var clause : clauses) {
            var ch = clause.getChannel();
            if (ch != null) {
                // if a channel is in error, closedForSend() will return that information
                var closedForSend = clause.getChannel().closedForSend();
                if (closedForSend instanceof ChannelError ce) {
                    return ce;
                }
            }
        }
        return null;
    }

    public static <T> SelectClause<T> defaultClause(T value) {
        return defaultClause(() -> value);
    }

    public static <T> SelectClause<T> defaultClause(Supplier<T> callback) {
        return new DefaultClause<>(callback);
    }
}

class SelectInstance {
    /**
     * Possible states:
     * - one of {@link SelectState}
     * - {@link Thread} to wake up
     * - {@link ChannelClosed}
     * - a {@link List} of clauses to re-register
     * - when selected, {@link SelectClause} (during registration) or {@link StoredSelectClause} (with suspension)
     */
    private volatile Object state = SelectState.REGISTERING;

    private static final VarHandle STATE;

    static {
        try {
            MethodHandles.Lookup l = MethodHandles.privateLookupIn(SelectInstance.class, MethodHandles.lookup());
            STATE = l.findVarHandle(SelectInstance.class, "state", Object.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * The content of the list will be written & read only by the main select thread. Hence, no synchronization is necessary.
     */
    private final List<StoredSelectClause> storedClauses;

    /**
     * The result of registering a clause, if it was selected immediately (during registration).
     * Only written & read only by the main select thread. Hence, no synchronization is necessary.
     */
    private Object resultSelectedDuringRegistration;

    SelectInstance(int clausesCount) {
        storedClauses = new ArrayList<>(clausesCount);
    }

    // registration

    /**
     * Register a clause in this select instance. Only one clause for each channel should be registered.
     *
     * @return {@code true}, if the registration was successful, and the clause has been stored. {@code false}, if the
     * channel is closed, or the clause has been immediately selected.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    <U> boolean register(SelectClause<U> clause) {
        // register the clause
        var result = clause.register(this);
        if (result instanceof StoredSelectClause ss) {
            // keeping the stored select to later call cleanup()
            storedClauses.add(ss);
            return true;
        } else if (result instanceof ChannelClosed cc) {
            // when setting the state, we might override another state:
            // - a list of clauses to re-register - there's no point in doing that anyway (since the channel is closed)
            // - another closed state (set concurrently)
            state = cc;
            return false;
        } else {
            // else: the clause was selected
            resultSelectedDuringRegistration = result; // used in checkStateAndWait() later
            // when setting the state, we might override another state:
            // - a list of clauses to re-register - there's no point in doing that anyway (since we already selected a clause)
            // - a closed state - the closure must have happened concurrently with registration; we give priority to immediate selects then
            state = clause;
            return false;
        }
    }

    // main loop

    /**
     * @param allRendezvous If channels for all clauses are rendezvous channels. In such a case, busy-looping is
     *                      initially used, instead of blocking.
     * @return Either the value returned by the selected clause (which can include {@link RestartSelectMarker#RESTART}),
     * or {@link ChannelClosed}, when any of the channels is closed.
     */
    Object checkStateAndWait(boolean allRendezvous) throws InterruptedException {
        while (true) {
            var currentState = state;
            if (currentState == SelectState.REGISTERING) {
                // registering done, waiting until a clause is selected - setting the thread to wake up as the state
                // we won't leave this case until the state is changed from Thread
                var currentThread = Thread.currentThread();
                if (STATE.compareAndSet(this, SelectState.REGISTERING, currentThread)) {
                    var spinIterations = allRendezvous ? Continuation.RENDEZVOUS_SPINS : 0;
                    while (state == currentThread) {
                        // same logic as in Continuation
                        if (spinIterations > 0) {
                            Thread.onSpinWait();
                            spinIterations -= 1;
                        } else {
                            LockSupport.park();

                            if (Thread.interrupted()) {
                                if (STATE.compareAndSet(this, currentThread, SelectState.INTERRUPTED)) {
                                    // since we changed the state, we know that none of the clauses will become completed
                                    cleanup(null);
                                    throw new InterruptedException();
                                } else {
                                    // another thread already changed the state; setting the interrupt status (so that
                                    // the next blocking operation throws), and continuing
                                    Thread.currentThread().interrupt();
                                }
                            }
                        }
                    }
                    // inspect the updated state in next iteration
                }
                // else: CAS unsuccessful, retry
            } else if (currentState instanceof List) {
                // moving the state back to registering
                if (STATE.compareAndSet(this, currentState, SelectState.REGISTERING)) {
                    //noinspection unchecked
                    for (var clause : (List<SelectClause<?>>) currentState) {
                        // cleaning up & removing the stored select for the clause which we'll re-register
                        var storedSelectsIterator = storedClauses.iterator();
                        while (storedSelectsIterator.hasNext()) {
                            var stored = storedSelectsIterator.next();
                            if (stored.getClause() == clause) {
                                stored.cleanup();
                                storedSelectsIterator.remove();
                                break;
                            }
                        }

                        if (!register(clause)) {
                            // channel is closed, or clause was selected - in both cases, no point in further
                            // re-registrations; the state should be appropriately updated
                            break;
                        }
                    }
                    // inspect the updated state in next iteration
                }
                // else: CAS unsuccessful, retry
            } else if (currentState instanceof SelectClause<?> selectedClause) {
                // clause selected during registration - result in `resultSelectedDuringRegistration`

                cleanup(selectedClause);

                // running the transformation at the end, after the cleanup is done, in case this throws any exceptions
                return selectedClause.transformedRawValue(resultSelectedDuringRegistration);
            } else if (currentState instanceof StoredSelectClause ss) {
                // clause selected with suspension - result in `StoredSelect.payload`

                var selectedClause = ss.getClause();
                cleanup(selectedClause);

                // running the transformation at the end, after the cleanup is done, in case this throws any exceptions
                return selectedClause.transformedRawValue(ss.getPayload());
            } else if (currentState instanceof ChannelClosed cc) {
                cleanup(null);
                return cc;
            } else {
                throw new IllegalStateException("Unknown state: " + currentState);
            }
        }
    }

    private void cleanup(SelectClause<?> selected) {
        // cleaning up of all the clauses that were registered, except for the selected one
        for (var stored : storedClauses) {
            if (stored.getClause() != selected) {
                stored.cleanup();
            }
        }
        storedClauses.clear();
    }

    // callbacks from select, that a clause is selected / the channel is closed

    /**
     * @return {@code true} if the given clause was successfully selected, {@code false} otherwise (a channel is closed,
     * another clause is selected, registration is in progress, select is interrupted).
     */
    boolean trySelect(StoredSelectClause storedSelectClause) {
        while (true) {
            var currentState = state;
            if (currentState == SelectState.REGISTERING) {
                if (STATE.compareAndSet(this, currentState, Collections.singletonList(storedSelectClause.getClause()))) {
                    return false; // concurrent clause selection is not possible during registration
                }
                // else: CAS unsuccessful, retry
            } else if (currentState instanceof List<?> clausesToReRegister) {
                // we need a new object for CAS
                var newClausesToReRegister = new ArrayList<SelectClause<?>>(clausesToReRegister.size() + 1);
                //noinspection unchecked
                newClausesToReRegister.addAll((Collection<? extends SelectClause<?>>) clausesToReRegister);
                newClausesToReRegister.add(storedSelectClause.getClause());
                if (STATE.compareAndSet(this, currentState, newClausesToReRegister)) {
                    return false; // concurrent clause selection is not possible during registration
                }
                // else: CAS unsuccessful, retry
            } else if (currentState instanceof SelectClause) {
                // already selected, will be cleaned up soon
                return false;
            } else if (currentState instanceof StoredSelectClause) {
                // already selected, will be cleaned up soon
                return false;
            } else if (currentState instanceof Thread t) {
                if (STATE.compareAndSet(this, currentState, storedSelectClause)) {
                    LockSupport.unpark(t);
                    return true;
                }
                // else: CAS unsuccessful, retry
            } else if (currentState == SelectState.INTERRUPTED) {
                // already interrupted, will be cleaned up soon
                return false;
            } else if (currentState instanceof ChannelClosed) {
                // closed, will be cleaned up soon
                return false;
            } else {
                throw new IllegalStateException("Unknown state: " + currentState);
            }
        }
    }

    /**
     * @return {@code true} when the given {@code channelClosed} has been set as the new state of the select.
     * {@code false}, if another clause has already been selected, or if the channel is already closed.
     */
    boolean channelClosed(ChannelClosed channelClosed) {
        while (true) {
            var currentState = state;
            if (currentState == SelectState.REGISTERING) {
                // the channel closed state will be discovered when there's a call to `checkStateAndWait` after registration completes
                if (STATE.compareAndSet(this, currentState, channelClosed)) {
                    return true;
                }
                // else: CAS unsuccessful, retry
            } else if (currentState instanceof List) {
                // same as above
                if (STATE.compareAndSet(this, currentState, channelClosed)) {
                    return true;
                }
                // else: CAS unsuccessful, retry
            } else if (currentState instanceof SelectClause) {
                // already selected
                return false;
            } else if (currentState instanceof StoredSelectClause) {
                // already selected
                return false;
            } else if (currentState instanceof Thread t) {
                if (STATE.compareAndSet(this, currentState, channelClosed)) {
                    LockSupport.unpark(t);
                    return true;
                }
                // else: CAS unsuccessful, retry
            } else if (currentState == SelectState.INTERRUPTED) {
                // already interrupted
                return false;
            } else if (currentState instanceof ChannelClosed) {
                // already closed
                return false;
            } else {
                throw new IllegalStateException("Unknown state: " + currentState);
            }
        }
    }
}

enum SelectState {
    REGISTERING,
    INTERRUPTED
}

//

/**
 * Used to keep information about a select instance that is stored in a channel, awaiting completion.
 */
class StoredSelectClause {
    private final SelectInstance select;
    private final Segment segment;
    private final int i;
    private final boolean isSender;
    private final SelectClause<?> clause;
    private Object payload;

    public StoredSelectClause(SelectInstance select, Segment segment, int i, boolean isSender, SelectClause<?> clause, Object payload) {
        this.select = select;
        this.segment = segment;
        this.i = i;
        this.isSender = isSender;
        this.clause = clause;
        this.payload = payload;
    }

    public SelectInstance getSelect() {
        return select;
    }

    public boolean isSender() {
        return isSender;
    }

    SelectClause<?> getClause() {
        return clause;
    }

    void cleanup() {
        clause.getChannel().cleanupStoredSelectClause(segment, i, isSender);
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}

enum RestartSelectMarker {
    RESTART
}

enum TimeoutMarker {
    INSTANCE
}
