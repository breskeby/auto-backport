package com.breskeby.rewrite.java.backport;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import java.util.UUID;

import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.rangeClosed;

@Value
@EqualsAndHashCode(callSuper = true)
public class FullQualifiedChangeMethodOwnerRecipe extends Recipe {

    @Option(displayName = "Fully-qualified Method pattern",
            description = "A fully-qualified class name of the type upon which the static method is defined.",
            example = "java.util.List of(..)")
    String originFullQualifiedClassname;

    @Option(displayName = "Fully-qualified Method pattern",
            description = "A fully-qualified class name of the type upon which the static method is defined.",
            example = "java.util.List of(..)")
    String originMethod;

    @Option(displayName = "Fully-qualified target type name",
            description = "A fully-qualified class name of the type upon which the static method is defined.",
            example = "org.acme.List.of")
    String targetFullQualifiedClassname;


    @Override
    public String getDisplayName() {
        return "FullQualifiedListOfBackportRecipe";
    }

    @Override
    public String getDescription() {
        return "Backport java.util.List.of() to Java 8 compliant plain java API";
    }

    @Override
    protected JavaVisitor<ExecutionContext> getVisitor() {
        return new FullQualifiedChangeMethodOwnerRecipe.BackportListOfVisitor(new MethodMatcher(originFullQualifiedClassname + " " + originMethod + "(..)"));
    }

    public class BackportListOfVisitor extends JavaVisitor<ExecutionContext> {
        private final MethodMatcher methodMatcher;

        public BackportListOfVisitor(MethodMatcher methodMatcher) {
            this.methodMatcher = methodMatcher;
        }

        @Override
        public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
            J j = super.visitMethodInvocation(method, executionContext);
            JavaType.Method type = method.getType();
            if (type != null && methodMatcher.matches(method)) {
                int paramCount = method.getArguments().size();
                String code = targetFullQualifiedClassname + "." + originMethod + "(" +
                        rangeClosed(1, paramCount).mapToObj(i -> "#{any()}").collect(joining(", ")) + ")";
                JavaTemplate listOfUsage = JavaTemplate.builder(this::getCursor, code).build();
                j = method.withTemplate(listOfUsage, method.getCoordinates().replace(), method.getArguments().toArray());
                maybeRemoveImport(originFullQualifiedClassname);
                trackChange(executionContext, j.getId());
            }
            return j;
        }

        private void trackChange(ExecutionContext executionContext, UUID id) {
            executionContext.putMessageInSet("Method change to " + getTargetFullQualifiedClassname(), id);
        }

    }
}
