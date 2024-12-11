package com.softwaremill.jox;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class FlowCompleteCallbacksTest {
    @Test
    void ensureOnCompleteRunsInCaseOfSuccess() throws Exception {
        // given
        AtomicBoolean didRun = new AtomicBoolean(false);
        Flow<Integer> f = Flows.fromValues(1, 2, 3)
                .onComplete(() -> didRun.set(true));
        assertFalse(didRun.get());
        
        // when
        f.runDrain();
        
        // then
        assertTrue(didRun.get());
    }

    @Test
    void ensureOnCompleteRunsInCaseOfError() {
        //given
        AtomicBoolean didRun = new AtomicBoolean(false);
        Flow<Integer> f = Flows.fromValues(1, 2, 3)
                .tap(i -> {throw new RuntimeException();})
                .onComplete(() -> didRun.set(true));
        assertFalse(didRun.get());
        
        // when
        assertThrows(RuntimeException.class, f::runDrain);
        
        // then
        assertTrue(didRun.get());
    }

    @Test
    void ensureOnDoneRunsInCaseOfSuccess() throws Exception {
        // given
        AtomicBoolean didRun = new AtomicBoolean(false);
        Flow<Integer> f = Flows.fromValues(1, 2, 3).onDone(() -> didRun.set(true));
        assertFalse(didRun.get());
        
        // when
        f.runDrain();
        
        // then
        assertTrue(didRun.get());
    }

    @Test
    void ensureOnDoneDoesNotRunInCaseOfError() {
        // given
        AtomicBoolean didRun = new AtomicBoolean(false);
        Flow<Integer> f = Flows.fromValues(1, 2, 3)
                .tap(i -> {throw new RuntimeException();})
                .onDone(() -> didRun.set(true));
        assertFalse(didRun.get());
        
        // when
        assertThrows(RuntimeException.class, f::runDrain);
        
        // then
        assertFalse(didRun.get());
    }

    @Test
    void ensureOnErrorDoesNotRunInCaseOfSuccess() throws Exception {
        // given
        AtomicBoolean didRun = new AtomicBoolean(false);
        Flow<Integer> f = Flows.fromValues(1, 2, 3)
                .onError(e -> didRun.set(true));
        assertFalse(didRun.get());
        
        // when
        f.runDrain();
        
        // then
        assertFalse(didRun.get());
    }

    @Test
    void ensureOnErrorRunsInCaseOfError() {
        // given
        AtomicBoolean didRun = new AtomicBoolean(false);
        Flow<Integer> f = Flows.fromValues(1, 2, 3)
                .tap(i -> {throw new RuntimeException();})
                .onError(e -> didRun.set(true));
        assertFalse(didRun.get());
        
        // when
        assertThrows(RuntimeException.class, f::runDrain);
        
        // then
        assertTrue(didRun.get());
    }
}
