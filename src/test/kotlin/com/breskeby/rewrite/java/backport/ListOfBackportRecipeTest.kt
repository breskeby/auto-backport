package com.breskeby.rewrite.java.backport

import org.junit.jupiter.api.Test
import org.openrewrite.Recipe
import org.openrewrite.config.Environment
import org.openrewrite.java.JavaRecipeTest

@Suppress("deprecation", "rawtypes")
class ListOfBackportRecipeTest : JavaRecipeTest {
    override val recipe: Recipe = Environment.builder()
            .scanRuntimeClasspath("com.breskeby.rewrite.java.backport")
            .build()
            .activateRecipes("com.breskeby.rewrite.java.backport.ListOfBackportRecipe")

    @Test
    fun rewritesSimpleListOf() = assertChanged(
            before = """
            import java.util.List;
            import 
            class Test {
                public void someMethod() {
                    List<String> ourList = List.of("entry1", "entry2");
                }
            }
        """,
            after = """
            import java.util.ArrayList;
            import java.util.Collections;
            import java.util.List;

            class Test {
                public void someMethod() {
                    List<String> ourList = listOf("entry1", "entry2");
                }

                private static <E> List<E> listOf(E e1, E e2) {
                    List<E> list = new ArrayList<E>();
                    list.add(e1);
                    list.add(e2);
                    return Collections.unmodifiableList(list);
                }
            }
        """
    )
//
//    @Test
//    fun mapsMapOfWithMethodInvocation() = assertChanged(
//            before = """
//            import java.util.Map;
//
//            class Test {
//                public void someMethod() {
//                    Map<String,String> ourMap = Map.of("key", "value".toUpperCase());
//                }
//            }
//        """,
//            after = """
//            import java.util.Collections;
//            import java.util.HashMap;
//            import java.util.Map;
//
//            class Test {
//                public void someMethod() {
//                    Map<String,String> ourMap = mapOf("key", "value".toUpperCase());
//                }
//
//                private static <K, V> Map<K, V> mapOf(K k1, V v1) {
//                    Map<K, V> map = new HashMap<K, V>();
//                    map.put(k1, v1);
//                    return Collections.unmodifiableMap(map);
//                }
//            }
//        """
//    )
//
//    @Test
//    fun mapsNestedMapOf() = assertChanged(
//            before = """
//            import java.util.Map;
//
//            class Test {
//                public void someMethod() {
//                    Map<String,Map<String,String>> ourMap = Map.of("key", Map.of("nestedKey", "nestedValue"));
//                }
//            }
//        """,
//            after = """
//            import java.util.Collections;
//            import java.util.HashMap;
//            import java.util.Map;
//
//            class Test {
//                public void someMethod() {
//                    Map<String,Map<String,String>> ourMap = mapOf("key", mapOf("nestedKey", "nestedValue"));
//                }
//
//                private static <K, V> Map<K, V> mapOf(K k1, V v1) {
//                    Map<K, V> map = new HashMap<K, V>();
//                    map.put(k1, v1);
//                    return Collections.unmodifiableMap(map);
//                }
//            }
//        """
//    )
//
//    @Test
//    fun multipleMapsNestedMapOf() = assertChanged(
//            before = """
//            import java.util.Map;
//
//            class Test {
//                public void someMethod() {
//                    Map<String,Map<String,String>> ourMap = Map.of("key", Map.of("nestedKey", "nestedValue"));
//                    Map<String,Map<String,String>> anotherMap = Map.of("key1", Map.of("nestedKey1", "nestedValue1"),
//                        "key2", Map.of("nestedKey11", "nestedValue11", "nestedKey22", "nestedValue22"));
//                }
//            }
//        """,
//            after = """
//            import java.util.Collections;
//            import java.util.HashMap;
//            import java.util.Map;
//
//            class Test {
//                public void someMethod() {
//                    Map<String,Map<String,String>> ourMap = mapOf("key", mapOf("nestedKey", "nestedValue"));
//                    Map<String,Map<String,String>> anotherMap = mapOf("key1", mapOf("nestedKey1", "nestedValue1"), "key2", mapOf("nestedKey11", "nestedValue11", "nestedKey22", "nestedValue22"));
//                }
//
//                private static <K, V> Map<K, V> mapOf(K k1, V v1) {
//                    Map<K, V> map = new HashMap<K, V>();
//                    map.put(k1, v1);
//                    return Collections.unmodifiableMap(map);
//                }
//
//                private static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
//                    Map<K, V> map = new HashMap<K, V>();
//                    map.put(k1, v1);
//                    map.put(k2, v2);
//                    return Collections.unmodifiableMap(map);
//                }
//            }
//        """
//    )
}
