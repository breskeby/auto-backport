package com.breskeby.rewrite.java.backport

import org.junit.jupiter.api.Test
import org.openrewrite.Recipe
import org.openrewrite.config.Environment
import org.openrewrite.java.JavaRecipeTest

@Suppress("deprecation", "rawtypes")
class MapOfBackportRecipeTest : JavaRecipeTest {
    override val recipe: Recipe = Environment.builder()
            .scanRuntimeClasspath("com.breskeby.rewrite.java.backport")
            .build()
            .activateRecipes("com.breskeby.rewrite.java.backport.MapOfBackportRecipe")

    @Test
    fun rewritesSimpleMapOf() = assertChanged(
            before = """
            import java.util.Map;

            class Test {
                public void someMethod() {
                    Map<String,String> ourMap = Map.of("key", "value");
                }
            }
        """,
            after = """
            import java.util.Collections;
            import java.util.HashMap;
            import java.util.Map;

            class Test {
                public void someMethod() {
                    Map<String,String> ourMap = mapOf("key", "value");
                }

                private static <K, V> Map<K, V> mapOf(K k1, V v1) {
                    Map<K, V> map = new HashMap<K, V>();
                    map.put(k1, v1);
                    return Collections.unmodifiableMap(map);
                }
            }
        """
    )

    @Test
    fun rewritesMapOfWithMethodInvocation() = assertChanged(
            before = """
            import java.util.Map;

            class Test {
                public void someMethod() {
                    Map<String,String> ourMap = Map.of("key", "value".toUpperCase());
                }
            }
        """,
            after = """
            import java.util.Collections;
            import java.util.HashMap;
            import java.util.Map;

            class Test {
                public void someMethod() {
                    Map<String,String> ourMap = mapOf("key", "value".toUpperCase());
                }

                private static <K, V> Map<K, V> mapOf(K k1, V v1) {
                    Map<K, V> map = new HashMap<K, V>();
                    map.put(k1, v1);
                    return Collections.unmodifiableMap(map);
                }
            }
        """
    )

    @Test
    fun rewritesNestedMapOf() = assertChanged(
            before = """
            import java.util.Map;

            class Test {
                public void someMethod() {
                    Map<String,Map<String,String>> ourMap = Map.of("key", Map.of("nestedKey", "nestedValue"));
                }
            }
        """,
            after = """
            import java.util.Collections;
            import java.util.HashMap;
            import java.util.Map;

            class Test {
                public void someMethod() {
                    Map<String,Map<String,String>> ourMap = mapOf("key", mapOf("nestedKey", "nestedValue"));
                }

                private static <K, V> Map<K, V> mapOf(K k1, V v1) {
                    Map<K, V> map = new HashMap<K, V>();
                    map.put(k1, v1);
                    return Collections.unmodifiableMap(map);
                }
            }
        """
    )

    @Test
    fun rewritesMultipleNestedMapOf() = assertChanged(
            before = """
            import java.util.Map;

            class Test {
                public void someMethod() {
                    Map<String,Map<String,String>> ourMap = Map.of("key", Map.of("nestedKey", "nestedValue"));
                    Map<String,Map<String,String>> anotherMap = Map.of("key1", Map.of("nestedKey1", "nestedValue1"), 
                        "key2", Map.of("nestedKey11", "nestedValue11", "nestedKey22", "nestedValue22"));
                }
            }
        """,
            after = """
            import java.util.Collections;
            import java.util.HashMap;
            import java.util.Map;

            class Test {
                public void someMethod() {
                    Map<String,Map<String,String>> ourMap = mapOf("key", mapOf("nestedKey", "nestedValue"));
                    Map<String,Map<String,String>> anotherMap = mapOf("key1", mapOf("nestedKey1", "nestedValue1"), "key2", mapOf("nestedKey11", "nestedValue11", "nestedKey22", "nestedValue22"));
                }
            
                private static <K, V> Map<K, V> mapOf(K k1, V v1) {
                    Map<K, V> map = new HashMap<K, V>();
                    map.put(k1, v1);
                    return Collections.unmodifiableMap(map);
                }
            
                private static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
                    Map<K, V> map = new HashMap<K, V>();
                    map.put(k1, v1);
                    map.put(k2, v2);
                    return Collections.unmodifiableMap(map);
                }
            }
        """
    )

    @Test
    fun handlesNestedClasses() = assertChanged(
            before = """
            import java.util.Map;

            class Test {
                public void someMethod() {
                    Map<String,Map<String,String>> ourMap = Map.of("key", Map.of("nestedKey", "nestedValue"));
                }
                
                public enum SomeEmom {
                    M1,
                    M2
                }
            }
        """,
            after = """
            import java.util.Collections;
            import java.util.HashMap;
            import java.util.Map;

            class Test {
                public void someMethod() {
                    Map<String,Map<String,String>> ourMap = mapOf("key", mapOf("nestedKey", "nestedValue"));
                }
                
                public enum SomeEmom {
                    M1,
                    M2
                }

                private static <K, V> Map<K, V> mapOf(K k1, V v1) {
                    Map<K, V> map = new HashMap<K, V>();
                    map.put(k1, v1);
                    return Collections.unmodifiableMap(map);
                }
            }
        """
    )
}
