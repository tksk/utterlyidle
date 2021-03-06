package com.googlecode.utterlyidle.rendering;

import com.googlecode.funclate.stringtemplate.EnhancedStringTemplateGroup;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.BasePath;
import com.googlecode.utterlyidle.Binding;
import com.googlecode.utterlyidle.FormParameters;
import com.googlecode.utterlyidle.MatchFailure;
import com.googlecode.utterlyidle.NamedParameter;
import com.googlecode.utterlyidle.Parameters;
import com.googlecode.utterlyidle.QueryParameters;
import com.googlecode.utterlyidle.Redirector;
import com.googlecode.utterlyidle.Renderer;
import org.antlr.stringtemplate.StringTemplateGroup;

import java.io.IOException;

import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Strings.EMPTY;
import static com.googlecode.totallylazy.URLs.packageUrl;

public class MatchFailureRenderer implements Renderer<MatchFailure> {
    private final BasePath basePath;
    private final Redirector redirector;

    public MatchFailureRenderer(BasePath basePath, final Redirector redirector) {
        this.basePath = basePath;
        this.redirector = redirector;
    }

    public String render(MatchFailure value) throws IOException {
        StringTemplateGroup group = new EnhancedStringTemplateGroup(packageUrl(getClass()));
        com.googlecode.funclate.Model model = model();
        model.add("base", basePath);
        model.add("status", value.status());

        Sequence<com.googlecode.funclate.Model> objects = value.matchesSoFar().filter(not(hidden())).map(bindingAsModel());

        model.add("resources", objects);

        return group.getInstanceOf("matchFailure", model.toMap()).toString();
    }

    private Function1<Binding, com.googlecode.funclate.Model> bindingAsModel() {
        return new Function1<Binding, com.googlecode.funclate.Model>() {
            @Override
            public com.googlecode.funclate.Model call(Binding binding) throws Exception {
                final String httpMethod = binding.httpMethod();
                Sequence<NamedParameter> parameters = binding.namedParameters();

                return model().
                        add("method", httpMethod.equals("*") ? "ANY" : httpMethod).
                        add("path", redirector.uriOf(binding).path()).
                        add("query", parameterAsModel(parameters.filter(where(parametersClass(), matches(QueryParameters.class))))).
                        add("form", parameterAsModel(parameters.filter(where(parametersClass(), matches(FormParameters.class)))));
            }
        };
    }

    private com.googlecode.funclate.Model model() {
        return com.googlecode.funclate.Model.mutable.model();
    }

    public static Predicate<? super Class> matches(Class aClass) {
        return Predicates.is(aClass);
    }

    public static Callable1<NamedParameter, Class<? extends Parameters<String, String, ?>>> parametersClass() {
        return new Callable1<NamedParameter, Class<? extends Parameters<String, String, ?>>>() {
            public Class<? extends Parameters<String, String, ?>> call(NamedParameter namedParameter) throws Exception {
                return namedParameter.parametersClass();
            }
        };
    }

    private com.googlecode.funclate.Model parameterAsModel(Sequence<NamedParameter> parameters) {
        com.googlecode.funclate.Model result = model();
        for (NamedParameter parameter : parameters) {
            result.add(parameter.name(), parameter.defaultValue().getOrElse(EMPTY));
        }
        return result;
    }

    public static Predicate<? super Binding> hidden() {
        return new Predicate<Binding>() {
            public boolean matches(Binding binding) {
                return binding.hidden();
            }
        };
    }
}
