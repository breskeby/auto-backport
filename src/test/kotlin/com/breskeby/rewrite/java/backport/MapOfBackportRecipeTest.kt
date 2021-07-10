package com.breskeby.rewrite.java.backport

import org.junit.jupiter.api.Test
import org.openrewrite.Recipe
import org.openrewrite.config.Environment
import org.openrewrite.java.JavaRecipeTest

@Suppress("deprecation", "rawtypes")
class MapsOfBackportRecipeTest : JavaRecipeTest {
    override val recipe: Recipe = Environment.builder()
            .scanRuntimeClasspath("com.breskeby.rewrite.java.backport")
            .build()
            .activateRecipes("com.breskeby.rewrite.java.backport.MapOfBackportRecipe")

    @Test
    fun mapsSimpleMapOf() = assertChanged(
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
    fun mapsMapOfWithMethodInvocation() = assertChanged(
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
    fun mapsNestedMapOf() = assertChanged(
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

//    @Test
//    fun handlesNestedMaps() = assertChanged(
//            before = """
//                package org.openrewrite.example;
//
//                import java.util.Map;
//
//                class A {
//                    public void someMethod(){
//                        Map<String,String> ourMap = Map.of("key", Map.of("nested", "value"));
//                    }
//                }
//            """,
//            after = """
//                package org.openrewrite.example;
//
//                import java.util.Collections;
//                import java.util.HashMap;
//                import java.util.Map;
//
//                class A {
//                    public void someMethod(){
//                        Map<String,String> ourMap = mapOf("key",  mapOf("nested", "value"));
//                    }
//
//                    private static <K, V> Map<K, V> mapOf(K k1, V v1) {
//                        Map<K, V> map = new HashMap<K, V>();
//                        map.put(k1, v1);
//                        return Collections.unmodifiableMap(map);
//                    }
//                }
//           """
//    )
//
//
//    @Test
//    fun mapsMultipleMapOf() = assertChanged(
//            before = """
//            import java.util.Map;
//
//            class A {
//                public void someMethod(){
//                    Map<String,String> ourMap = Map.of("key", "value");
//                    Map<String,String> someOtherMap = Map.of(String.valueOf(1), String.valueOf(2));
//                }
//            }""",
//            after = """import java.util.Collections;
//            import java.util.Map;
//            import java.util.HashMap;
//
//            class A {
//                public void someMethod(){
//                    Map<String,String> ourMap = mapOf("key",  "value");
//                    Map<String,String> ourMap = mapOf(String.valueOf(1),  String.valueOf(2));
//                }
//
//                private static <K, V> Map<K, V> mapOf(K k1, V v1) {
//                    Map<K, V> map = new HashMap<K, V>();
//                    map.put(k1, v1);
//                    return Collections.unmodifiableMap(map);
//                }
//            }"""
//    )
}
