package com.softwaremill.jox.structured;

import java.util.concurrent.Callable;

public interface Decorator {
    <T> Callable<T> decorate(Callable<T> f);
}
