package com.breskeby.rewrite.java.backport;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Statement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MapOfBackportRecipe extends Recipe {

    private static final MethodMatcher MAP_OF_MATCHER = new MethodMatcher("java.util.Map of(..)");

    @Override
    public String getDisplayName() {
        return "Backport Maps.of() to Java 8 compliant plain java API";
    }

    @Override
    protected JavaVisitor<ExecutionContext> getVisitor() {
        // getVisitor() should always return a new instance of the visitor to avoid any state leaking between cycles
        return new BackportMapsOfVisitor();
    }

    public class BackportMapsOfVisitor extends JavaIsoVisitor<ExecutionContext> {
        private final JavaTemplate mapOfTwoParamsTemplate = JavaTemplate.builder(this::getCursor,
                "private static <K, V> Map<K, V> mapOf(K k1, V v1) {" +
                        "Map<K, V> map = new HashMap<K,V>();" +
                        "map.put(k1, v1);" +
                        "return Collections.unmodifiableMap(map)" +
                        "}")
                .imports("java.util.Collections")
                .imports("java.util.HashMap").build();

        private final JavaTemplate mapOfFouraramsTemplate = JavaTemplate.builder(this::getCursor,
                "private static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {" +
                        "Map<K, V> map = new HashMap<K,V>();" +
                        "map.put(k1, v1);" +
                        "map.put(k2, v2);" +
                        "return Collections.unmodifiableMap(map)" +
                        "}")
                .imports("java.util.Collections")
                .imports("java.util.HashMap").build();
        private final JavaTemplate mapOfUsage = JavaTemplate.builder(this::getCursor, "mapOf(#{any()}, #{any()})").build();

        private boolean mapOfMethodNeeded = false;
        private JavaType.FullyQualified classType;

        @Override
        public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
            method = super.visitMethodInvocation(method, executionContext);
            JavaType.Method type = method.getType();
            if (type != null && MAP_OF_MATCHER.matches(method)) {
                mapOfMethodNeeded = true;
                JavaType.Parameterized parameterized = (JavaType.Parameterized) method.getType().getDeclaringType();
                JavaType.Parameterized build = JavaType.Parameterized.build(classType, parameterized.getTypeParameters());
                JavaType.Method mapOfType = method.getType().withName("mapOf").withDeclaringType(build);
                method = method.withTemplate(mapOfUsage, method.getCoordinates().replace(), method.getArguments().get(0), method.getArguments().get(1));
                return method.withType(mapOfType);
            }
            return method;
        }

        @Override
        public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext executionContext) {
            classType = classDecl.getType();
            J.ClassDeclaration cd = super.visitClassDeclaration(classDecl, executionContext);
            if (mapOfMethodNeeded) {
                // Check if the class already has a method named "mapOf" so we don't incorrectly add a second "mapOf" method
                boolean helloMethodAlreadyExists = classDecl.getBody().getStatements().stream()
                        .filter(statement -> statement instanceof J.MethodDeclaration)
                        .map(J.MethodDeclaration.class::cast)
                        .anyMatch(methodDeclaration -> methodDeclaration.getName().getSimpleName().equals("mapOf"));

                if (helloMethodAlreadyExists == false) {
                    // Interpolate the fullyQualifiedClassName into the template and use the resulting AST to update the class body
                    cd = cd.withBody(
                            cd.getBody().withTemplate(
                                    mapOfTwoParamsTemplate,
                                    cd.getBody().getCoordinates().lastStatement()
                            ));
                    maybeAddImport("java.util.Map");
                    maybeAddImport("java.util.HashMap");
                    maybeAddImport("java.util.Collections");

                }
            }

            return cd;
        }
    }
}

/***
 *
 *     static {
 *         ARCHITECTURES = Map.of(
 *                 "amd64", new Arch(0xC000003E, 0x3FFFFFFF, 57, 58, 59, 322, 317),
 *                 "aarch64", new Arch(0xC00000B7, 0xFFFFFFFF, 1079, 1071, 221, 281, 277));
 *     }
 *
 *     to
 *
 *
 *     static {
 *         Map<String,Arch> m = new HashMap<>();
 *         m.put("amd64", new Arch(0xC000003E, 0x3FFFFFFF, 57, 58, 59, 322, 317));
 *         m.put("aarch64",  new Arch(0xC00000B7, 0xFFFFFFFF, 1079, 1071, 221, 281, 277));
 *         ARCHITECTURES = Collections.unmodifiableMap(m);
 *     }
 */
