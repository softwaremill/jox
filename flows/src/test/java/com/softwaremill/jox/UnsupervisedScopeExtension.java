package com.softwaremill.jox;

import com.softwaremill.jox.structured.Scopes;
import com.softwaremill.jox.structured.UnsupervisedScope;
import org.junit.jupiter.api.extension.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class UnsupervisedScopeExtension implements ParameterResolver, InvocationInterceptor {

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        Scopes.unsupervised(scope -> {
            try {
                extensionContext.getRequiredTestMethod().invoke(extensionContext.getRequiredTestInstance(), scope);
            } catch (InvocationTargetException e) {
                if (Objects.requireNonNull(e.getTargetException()) instanceof Error) {
                    throw (Error) e.getTargetException();
                }
                throw new IllegalStateException("Unexpected value: " + e.getTargetException());
            }
            invocation.skip();
            return null;
        });
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType().isAssignableFrom(UnsupervisedScope.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
//        required by ParameterResolver but actual value is passed in interceptTestMethod
        return null;
    }
}