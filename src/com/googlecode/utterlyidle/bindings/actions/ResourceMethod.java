package com.googlecode.utterlyidle.bindings.actions;

import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Value;

import java.lang.reflect.Method;

public class ResourceMethod implements ActionMetaData, Value<Method> {
    private final Method value;

    public ResourceMethod(Method value) {
        this.value = value;
    }

    @Override
    public Method value() {
        return value;
    }

    public static class constructors {
        public static ResourceMethod resourceMethod(Method method) {
            return new ResourceMethod(method);
        }
    }

    public static class functions {
        public static Predicate<ResourceMethod> isForMethod(final Method method) {
            return new Predicate<ResourceMethod>() {
                @Override
                public boolean matches(ResourceMethod resourceMethod) {
                    return resourceMethod.value.equals(method);
                }
            };
        }
    }
}
