package com.googlecode.utterlyidle.dsl;

import com.googlecode.utterlyidle.ApplicationBuilder;
import com.googlecode.utterlyidle.RequestBuilder;
import com.googlecode.utterlyidle.Status;
import org.junit.Test;

import static com.googlecode.utterlyidle.ApplicationBuilder.application;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.dsl.BindingBuilder.definedParam;
import static com.googlecode.utterlyidle.dsl.BindingBuilder.get;
import static com.googlecode.utterlyidle.dsl.BindingBuilder.queryParam;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DslTest {
    @Test
    public void supportsGet() throws Exception {
        ApplicationBuilder application = application().add(get("/bar").resource(method(on(Bar.class).hello())));
        assertThat(application.responseAsString(RequestBuilder.get("/bar")), is("Hello"));
    }

    @Test
    public void supportsGetWithParameters() throws Exception {
        ApplicationBuilder application = application().add(get("/bar").resource(method(on(Foo.class).say(queryParam(String.class, "value")))));
        assertThat(application.responseAsString(RequestBuilder.get("/bar").withQuery("value", "Dan")), is("Hello Dan"));
    }

    @Test
    public void supportsGetWithDefaultValue() throws Exception {
        ApplicationBuilder application = application().add(get("/hello").resource(method(on(Foo.class).say(queryParam(String.class, "name", "Matt")))));
        assertThat(application.responseAsString(RequestBuilder.get("/hello")), is("Hello Matt"));
        assertThat(application.responseAsString(RequestBuilder.get("/hello").withQuery("name", "Dan")), is("Hello Dan"));
    }

    @Test
    public void supportsDefinedParameter() throws Exception {
        ApplicationBuilder application = application().add(get("/hello").resource(method(on(Foo.class).say(definedParam("Matt")))));
        assertThat(application.responseAsString(RequestBuilder.get("/hello")), is("Hello Matt"));
        assertThat(application.responseAsString(RequestBuilder.get("/hello").withQuery("name", "Dan")), is("Hello Matt"));
    }

    @Test
    public void supportsGetWithMultipleParameters() throws Exception {
        ApplicationBuilder application = application().add(get("/bar").resource(method(on(Bob.class).say(queryParam(String.class, "firstName"), queryParam(String.class, "lastName")))));
        assertThat(application.responseAsString(RequestBuilder.get("/bar").withQuery("firstName", "Dan").withQuery("lastName", "Bodart")), is("Hello Dan Bodart"));
    }

    @Test
    public void supportsProduces() throws Exception {
        ApplicationBuilder application = application().add(get("/bar").produces("text/html", "text/xml").resource(method(on(Bar.class).hello())));
        assertThat(application.responseAsString(RequestBuilder.get("/bar").accepting("text/html")), is("Hello"));
        assertThat(application.responseAsString(RequestBuilder.get("/bar").accepting("text/xml")), is("Hello"));
        assertThat(application.handle(RequestBuilder.get("/bar").accepting("text/plain")).status(), is(Status.NOT_ACCEPTABLE));
    }


    public static class Bar {
        public String hello() {
            return "Hello";
        }
    }

    public static class Foo {
        public String say(String value) {
            return "Hello " + value;
        }
    }

    public static class Bob {
        public String say(String first, String last) {
            return "Hello " + first + " " + last;
        }
    }

}