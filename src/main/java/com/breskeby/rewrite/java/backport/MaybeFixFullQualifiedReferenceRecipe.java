package com.breskeby.rewrite.java.backport;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.joining;

@Value
@EqualsAndHashCode(callSuper = true)
public class MaybeFixFullQualifiedReferenceRecipe extends Recipe {

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
        return "HandleSameClassnamesRecipe";
    }

    @Override
    public String getDescription() {
        return "Backport java.util.List.of() to Java 8 compliant plain java API";
    }

    @Override
    protected JavaVisitor<ExecutionContext> getVisitor() {
        String unqualifiedIdentifier = targetFullQualifiedClassname.substring(targetFullQualifiedClassname.lastIndexOf('.') + 1);
        return new Visitor(unqualifiedIdentifier);
    }

    public class Visitor extends JavaVisitor<ExecutionContext> {

        private String unqualifiedIdentifier;
        private boolean hasOriginImport;

        public Visitor(String unqualifiedIdentifier) {
            this.unqualifiedIdentifier = unqualifiedIdentifier;
        }

        @Override
        public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
            Set<Object> processed = executionContext.getMessage("Method change to " + getTargetFullQualifiedClassname(), Collections.emptySet());
            boolean changedMadeBefore = processed.contains(method.getId());
            J.MethodInvocation m = (J.MethodInvocation) super.visitMethodInvocation(method, executionContext);
            if(changedMadeBefore && hasOriginImport == false && m.getSelect() instanceof J.FieldAccess && ((J.FieldAccess) m.getSelect()).isFullyQualifiedClassReference(targetFullQualifiedClassname)) {
                Expression select = m.getSelect();
                JavaType javaType = JavaType.buildType(targetFullQualifiedClassname);
                J.Identifier list = J.Identifier.build(select.getId(), select.getPrefix(), select.getMarkers(), unqualifiedIdentifier, javaType);
                m = m.withSelect(list);
                maybeAddImport(targetFullQualifiedClassname);
            }
            return m;
        }


        @Override
        public J visitCompilationUnit(J.CompilationUnit cu, ExecutionContext executionContext) {
            J.CompilationUnit c = cu;

            hasOriginImport = c.getImports().stream().anyMatch(i -> i.isFromType(originFullQualifiedClassname));
            return super.visitCompilationUnit(cu, executionContext);
        }
    }


}
