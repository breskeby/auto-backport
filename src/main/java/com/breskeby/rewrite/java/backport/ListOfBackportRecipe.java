package com.breskeby.rewrite.java.backport;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.rangeClosed;

public class ListOfBackportRecipe extends Recipe {

    private static final MethodMatcher MAP_OF_MATCHER = new MethodMatcher("java.util.List of(..)");
    public static final String BACKPORT_LIST_OF_METHOD_NAME = "listOf";

    @Override
    public String getDisplayName() {
        return "ListOfBackportRecipe";
    }

    @Override
    public String getDescription() {
        return "Backport java.util.List.of() to Java 8 compliant plain java API";
    }

    @Override
    protected JavaVisitor<ExecutionContext> getVisitor() {
        return new BackportListOfVisitor();
    }

    public class BackportListOfVisitor extends JavaIsoVisitor<ExecutionContext> {
        private JavaType.FullyQualified classType;
        private Map<UUID, Set<Integer>> usedMethodParamSizes = new HashMap<>();
        private UUID currentClassId;

        @Override
        public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
            method = super.visitMethodInvocation(method, executionContext);
            JavaType.Method type = method.getType();
            if (type != null && MAP_OF_MATCHER.matches(method)) {
                int paramCount = method.getArguments().size();
                usedMethodParamSizes.compute(currentClassId, (uuid, integers) -> {
                    if (integers == null) {
                        integers = new HashSet<>();
                    }
                    integers.add(paramCount);
                    return integers;
                });
                String code = BACKPORT_LIST_OF_METHOD_NAME + "(" +
                        rangeClosed(1, paramCount).mapToObj(i -> "#{any()}").collect(joining(", ")) + ")";
                JavaTemplate listOfUsage = JavaTemplate.builder(this::getCursor, code).build();
                JavaType.Parameterized parameterized = (JavaType.Parameterized) method.getType().getDeclaringType();
                JavaType.Parameterized build = JavaType.Parameterized.build(classType, parameterized.getTypeParameters());
                JavaType.Method listOfType = method.getType().withName(BACKPORT_LIST_OF_METHOD_NAME).withDeclaringType(build);
                method = method.withTemplate(listOfUsage, method.getCoordinates().replace(), method.getArguments().toArray());
                return method.withType(listOfType);
            }
            return method;
        }


        @Override
        public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration cd, ExecutionContext executionContext) {
            classType = cd.getType();
            currentClassId = cd.getId();
            cd = super.visitClassDeclaration(cd, executionContext);
            for (Integer usedMethodParamSize : usedMethodParamSizes.getOrDefault(cd.getId(), Collections.emptySet())) {
                cd = maybeListOfImplementation(cd, usedMethodParamSize);
            }
            return cd;
        }

        private J.ClassDeclaration maybeListOfImplementation(J.ClassDeclaration cd, Integer usedMethodParamSize) {
            // Check if the class already has a method named "listOf" so we don't incorrectly add a second "listOf" method
            boolean methodAlreadyExists = cd.getBody().getStatements().stream()
                    .filter(statement -> statement instanceof J.MethodDeclaration)
                    .map(J.MethodDeclaration.class::cast)
                    .anyMatch(md -> md.getName().getSimpleName().equals(BACKPORT_LIST_OF_METHOD_NAME) && md.getParameters().size() == usedMethodParamSize);

            if (methodAlreadyExists == false) {
                cd = cd.withBody(
                        cd.getBody().withTemplate(
                                listOfTemplate(usedMethodParamSize),
                                cd.getBody().getCoordinates().lastStatement()
                        ));
                maybeAddImport("java.util.Collections");
                maybeAddImport("java.util.ArrayList");
            }
            return cd;
        }

        private JavaTemplate listOfTemplate(Integer paramsCount) {
            String parameterString = rangeClosed(1, paramsCount).mapToObj(i -> ("E e" + i))
                    .collect(joining(", "));
            String listOfOperationString = rangeClosed(1, paramsCount).mapToObj(i -> ("list.add(e" + i + ");"))
                    .collect(joining("\n"));
            return JavaTemplate.builder(this::getCursor,
                    "private static <E> List<E> listOf(" + parameterString + ") {" +
                            "List<E> list = new ArrayList<E>();" +
                            listOfOperationString +
                            "return Collections.unmodifiableList(list)" +
                            "}")
                    .imports("java.util.ArrayList")
                    .imports("java.util.Collections").build();
        }
    }
}
