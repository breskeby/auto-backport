package com.breskeby.rewrite.java.backport;

import org.openrewrite.Option;
import org.openrewrite.Recipe;

public class ChangeMethodOwnerRecipe extends Recipe {

    @Option(displayName = "Fully-qualified Method pattern",
            description = "A fully-qualified class name of the type upon which the static method is defined.",
            example = "java.util.List of(..)")
    String originFullQualifiedClassname;

    @Option(displayName = "Fully-qualified Method pattern",
            description = "A fully-qualified class name of the type upon which the static method is defined.",
            example = "java.util.List of(..)")
    String methodName;

    @Option(displayName = "Fully-qualified target type name",
            description = "A fully-qualified class name of the type upon which the static method is defined.",
            example = "org.acme.List.of")
    String targetFullQualifiedClassname;

    @Override
    public String getDisplayName() {
        return "Change Method Owner Recipe chain";
    }

    public ChangeMethodOwnerRecipe(String originFullQualifiedClassname, String methodName, String targetFullQualifiedClassname) {
        this.originFullQualifiedClassname = originFullQualifiedClassname;
        this.methodName = methodName;
        this.targetFullQualifiedClassname = targetFullQualifiedClassname;

        doNext(new FullQualifiedChangeMethodOwnerRecipe(originFullQualifiedClassname, methodName, targetFullQualifiedClassname));
        doNext(new MaybeFixFullQualifiedReferenceRecipe(originFullQualifiedClassname, methodName, targetFullQualifiedClassname));
    }
}
