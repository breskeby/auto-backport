import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
    `java-library`
    `maven-publish`
    id("org.jetbrains.kotlin.jvm") version "1.5.10"
    id("org.openrewrite.rewrite") version "latest.release"
}

rewrite {
    rewriteVersion = "latest.integration"
    activeRecipe("org.openrewrite.java.format.AutoFormat", "org.openrewrite.java.cleanup.Cleanup")
}

group = "com.breskeby.rewrite"
description = "Automatically backport Java to Java 8 compliant"

repositories {
    if(!project.hasProperty("releasing")) {
        mavenLocal()
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
    }
    mavenCentral()
}

configurations.all {
    resolutionStrategy {
        cacheChangingModulesFor(0, TimeUnit.SECONDS)
        cacheDynamicVersionsFor(0, TimeUnit.SECONDS)
    }
}

val rewriteVersion = if(project.hasProperty("releasing")) {
    "latest.release"
} else {
    "latest.integration"
}

dependencies {
    compileOnly("org.projectlombok:lombok:latest.release")
    annotationProcessor("org.projectlombok:lombok:latest.release")

    implementation("org.openrewrite:rewrite-java:${rewriteVersion}")
    implementation("org.openrewrite:rewrite-maven:${rewriteVersion}")
    runtimeOnly("org.openrewrite:rewrite-java-11:${rewriteVersion}")

    // eliminates "unknown enum constant DeprecationLevel.WARNING" warnings from the build log
    // see https://github.com/gradle/kotlin-dsl-samples/issues/1301 for why (okhttp is leaking parts of kotlin stdlib)
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("org.junit.jupiter:junit-jupiter-api:latest.release")
    testImplementation("org.junit.jupiter:junit-jupiter-params:latest.release")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:latest.release")

    testImplementation("org.openrewrite:rewrite-test:${rewriteVersion}")

    testImplementation("org.assertj:assertj-core:latest.release")

    testImplementation("com.google.guava:guava:29.0-jre")

    testRuntimeOnly("org.openrewrite:rewrite-java-11:${rewriteVersion}")
    testRuntimeOnly("org.openrewrite:rewrite-java-8:${rewriteVersion}")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    jvmArgs = listOf("-XX:+UnlockDiagnosticVMOptions", "-XX:+ShowHiddenFrames", "--add-exports", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED", "--add-exports", "jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
            "--add-exports", "jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
            "--add-exports", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
            "--add-exports", "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
            "--add-exports", "jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED")
}

tasks.named<JavaCompile>("compileJava") {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()

    options.isFork = true
    options.forkOptions.executable = "javac"
    options.compilerArgs.addAll(listOf("--release", "8"))
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.withType(KotlinCompile::class.java).configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }

    doFirst {
        destinationDir.mkdirs()
    }
}
