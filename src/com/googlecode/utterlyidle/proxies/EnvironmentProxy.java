package com.googlecode.utterlyidle.proxies;

import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.match;
import com.googlecode.totallylazy.regex.Regex;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;

import static com.googlecode.totallylazy.Maps.find;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.matches;
import static com.googlecode.totallylazy.Strings.endsWith;
import static com.googlecode.totallylazy.Strings.equalIgnoringCase;
import static com.googlecode.totallylazy.Uri.functions.uri;
import static com.googlecode.totallylazy.regex.Regex.regex;

public class EnvironmentProxy implements ProxyFor {
    private final Option<Proxy> http_proxy;
    private final Sequence<Predicate<String>> noProxy;

    private EnvironmentProxy(final Map<String, String> env) {
        http_proxy = httpProxy(env).flatMap(uri.optional()).map(proxyFor);
        noProxy = find(env, equalIgnoringCase("no_proxy")).toSequence().
                flatMap(split(regex(","))).
                map(toPredicate).
                cons(is("127.0.0.1")).
                cons(is("localhost")).
                realise();
    }

    public static Option<String> httpProxy(final Map<String, String> env) {
        return find(env, equalIgnoringCase("http_proxy"));
    }

    private final Regex wildcard = Regex.regex("\\*(.+)");

    private final Mapper<String, Predicate<String>> toPredicate = new Mapper<String, Predicate<String>>() {
        @Override
        public Predicate<String> call(final String value) throws Exception {
            return new match<String, Predicate<String>>(wildcard) {
                Predicate<String> value(String suffix) {
                    return endsWith(suffix);
                }
            }.apply(value).getOrElse(is(value));
        }
    };

    public static ProxyFor environmentProxy(final Map<String, String> env) {
        return HttpProxy.httpProxy(new EnvironmentProxy(env));
    }

    public static ProxyFor environmentProxy() {
        return environmentProxy(System.getenv());
    }

    @Override
    public Option<Proxy> proxyFor(final Uri uri) {
        if (noProxy.exists(matches(uri.host()))) return none();
        return http_proxy;
    }

    private Proxy createProxy(final Uri uri) {
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(uri.host(), port(uri.authority())));
    }

    private final Mapper<Uri, Proxy> proxyFor = new Mapper<Uri, Proxy>() {
        @Override
        public Proxy call(final Uri value) throws Exception {
            return createProxy(value);
        }
    };

    private final Regex r = regex(".*:(\\d+)");

    private int port(final String authority) {
        return Integer.valueOf(some(r.match(authority).group(1)).getOrElse("80"));
    }

    private static Mapper<String, Sequence<String>> split(final Regex r) {
        return new Mapper<String, Sequence<String>>() {
            @Override
            public Sequence<String> call(final String s) throws Exception {
                return r.split(s);
            }
        };
    }
}