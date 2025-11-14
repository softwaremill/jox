package com.softwaremill.jox.structured;

public record ExternalRunner(ActorRef<ExternalScheduler> scheduler) {
    /**
     * Allows to runs the given function asynchronously, in the scope of the concurrency scope in
     * which this runner was created.
     *
     * <p>`f` should return promptly, not to obstruct execution of other scheduled functions.
     * Typically, it should start a background fork.
     */
    public void runAsync(ThrowingConsumer<Scope> f) {
        try {
            scheduler.ask(
                    s -> {
                        s.run(f);
                        return null;
                    });
        } catch (Exception e) {
            SneakyThrows.sneakyThrow(e);
        }
    }
}
