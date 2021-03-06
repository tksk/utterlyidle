package com.googlecode.utterlyidle.handlers;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;

import static com.googlecode.totallylazy.Pair.pair;

public class HandlerRule {
    private final Predicate<? super Pair<Request, Response>> predicate;
    private final Object handler;

    private HandlerRule(Predicate<? super Pair<Request, Response>> predicate, Object handler) {
        this.predicate = predicate;
        this.handler = handler;
    }

    public static HandlerRule rule(Predicate<? super Pair<Request, Response>> predicate, Object handler) {
        return new HandlerRule(predicate, handler);
    }

    public static Predicate<HandlerRule> matches(final Request request, final Response response) {
        return new Predicate<HandlerRule>() {
            public boolean matches(HandlerRule handlerRule) {
                return handlerRule.predicate.matches(pair(request, response));
            }
        };
    }

    public static Callable1<? super HandlerRule, Object> getHandlerFromRule() {
        return new Callable1<HandlerRule, Object>() {
            public Object call(HandlerRule handlerRule) throws Exception {
                return handlerRule.handler;
            }
        };
    }

    public static Callable1<? super Pair<Request, Response>, Object> entity() {
        return entity(Object.class);
    }

    public static Callable1<? super Pair<Request, Response>, Status> status() {
        return new Callable1<Pair<Request, Response>, Status>() {
            public Status call(Pair<Request, Response> pair) throws Exception {
                return pair.second().status();
            }
        };
    }

    public static Callable1<Pair<Request, Response>, String> method() {
        return new Callable1<Pair<Request, Response>, String>() {
            @Override
            public String call(final Pair<Request, Response> pair) throws Exception {
                return pair.first().method();
            }
        };
    }

    public static <T> Callable1<? super Pair<Request, Response>, T> entity(final Class<T> aClass) {
        return new Callable1<Pair<Request, Response>, T>() {
            public T call(Pair<Request, Response> pair) throws Exception {
                if (pair.second().entity().value() != null && !aClass.isAssignableFrom(pair.second().entity().value().getClass())) {
                    return null;
                }
                return aClass.cast(pair.second().entity().value());
            }
        };
    }

}
