package com.breskeby.rewrite.java.backport

import org.junit.jupiter.api.Test
import org.openrewrite.java.JavaRecipeTest

@Suppress("deprecation", "rawtypes")
class FullQualifiedListOfBackportRecipeTest : JavaRecipeTest {
    @Test
    fun rewritesSimpleListOf() = assertChanged(
            skipEnhancedTypeValidation = true,
            dependsOn = arrayOf(
            """
            package com.breskeby.rewrite;
            
            import java.util.Arrays;

            public class List {
                public static java.util.List of(String value1, String value2) {
                    return Arrays.asList(value1, value2);
                }
            }"""
            ),
            recipe = ChangeMethodOwnerRecipe("java.util.List", "of", "com.breskeby.rewrite.List"),

            before = """
            import java.util.List;
            
            class Test {
                public void someMethod() {
                    List<String> ourList = List.of("entry1", "entry2");
                }
            }
            """,
            after = """
            import java.util.List;
            
            class Test {
                public void someMethod() {
                    List<String> ourList = com.breskeby.rewrite.List.of("entry1", "entry2");
                }
            }
            """,
    )

    @Test
    fun rewritesSimpleMapOf() = assertChanged(
            skipEnhancedTypeValidation = true,
            dependsOn = arrayOf(
                    """
            package com.breskeby.rewrite;
            
            import java.util.Arrays;

            public class Map {
                public static java.util.Map of(String key, String value) {
                    return java.util.Map.of(key, value);
                }
            }"""
            ),
            recipe = ChangeMethodOwnerRecipe("java.util.Map", "of", "com.breskeby.rewrite.Map"),

            before = """
            import java.util.Map;
            
            class Test {
                public void someMethod() {
                    Map<String, String> myMap = Map.of("key", "value");
                }
            }
            """,
            after = """
            import java.util.Map;
            
            class Test {
                public void someMethod() {
                    Map<String, String> myMap = com.breskeby.rewrite.Map.of("key", "value");
                }
            }
            """
    )

    @Test
    fun rewritesListOfWithNoListImport() = assertChanged(
            skipEnhancedTypeValidation = true,
            dependsOn = arrayOf(
                    """
            package com.breskeby.rewrite;
            
            import java.util.Arrays;

            public class List {
                public static java.util.List of(String value1, String value2) {
                    return Arrays.asList(value1, value2);
                }
            }"""
            ),
            recipe = ChangeMethodOwnerRecipe("java.util.List", "of", "com.breskeby.rewrite.List"),
            before = """
            import java.util.Collection;
            import java.util.List;

            class Test {
                public void someMethod() {
                    Collection<String> ourList = List.of("entry1", "entry2");
                }
            }
            """,
            after = """
            import com.breskeby.rewrite.List;

            import java.util.Collection;
            
            class Test {
                public void someMethod() {
                    Collection<String> ourList = List.of("entry1", "entry2");
                }
            }
            """,
    )

    @Test
    fun keepsUnchangedFullQualified() = assertUnchanged(
            dependsOn = arrayOf(
                    """
            package com.breskeby.rewrite;
            
            import java.util.Arrays;

            public class List {
                public static java.util.List of(String value1, String value2) {
                    return Arrays.asList(value1, value2);
                }
            }"""
            ),
            recipe = ChangeMethodOwnerRecipe("java.util.List", "of", "com.breskeby.rewrite.List"),
            before = """
            import java.util.Collection;
            
            class Test {
                public void someMethod() {
                    Collection<String> ourList = com.breskeby.rewrite.List.of("entry1", "entry2");
                }
            }"""
    )

}
