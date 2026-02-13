# 1. Channel Size Estimation Is Not Supported

Date: 2026-02-13

## Status

Accepted

## Context

A common monitoring/observability use case is knowing how many items are currently buffered in a channel. PR #254 proposed adding `Channel.estimateSize()` to support this.

Jox channels use a lock-free segment-based algorithm. The algorithm maintains two monotonically increasing atomic counters: `senders` and `receivers`. A naive size estimate would be `senders - receivers`, but these counters track **attempts**, not completions.

When a send is interrupted (e.g., channel is full, sender blocks, thread is interrupted), the `senders` counter stays permanently incremented — it is never decremented. This means N interrupted sends cause a permanent inflation of N in the estimate. This is not transient concurrency imprecision; it is a structural deficiency of using attempt counters for size estimation.

Fixing this properly would require adding completion counters (additional `volatile long` fields incremented on each successful send/receive), adding an overhead per operation to the hot path — the core performance-critical code that every send and receive must execute.

## Decision

Do not expose size estimation on channels.

Kotlin's kotlinx.coroutines — which uses the same algorithm — deliberately does not expose any size method on `Channel`. Their stance: channels are communication primitives, not inspectable containers. The lock-free segment design makes reliable size estimation fundamentally impractical without hot-path performance cost.

For monitoring, Kotlin relies on the coroutines debug agent (tracking coroutine lifecycle — suspended/running) rather than channel internals.

Users who need channel-level metrics should build external solutions (e.g., wrapping send/receive with their own counters, or application-level metrics).

## Consequences

- **No misleading API**: users won't rely on an estimate that silently drifts from reality after interruptions.
- **No hot-path performance cost**: avoids adding atomic increments to every send/receive operation.
- **No built-in channel monitoring**: users needing buffer-level metrics must implement them externally.
- **Consistent with Kotlin's approach**: aligns with the kotlinx.coroutines design decision for the same algorithm.
