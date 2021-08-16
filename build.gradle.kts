import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    `maven-publish`
    `signing`
    id("org.jetbrains.kotlin.jvm") version "1.5.10"
    id("org.openrewrite.rewrite") version "latest.release"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

group = "com.breskeby.rewrite"
version = "1.0.1-SNAPSHOT"
description = "Custom java recipes to be used by the open rewrite tool"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

val rewriteVersion = "7.10.0"
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

    testImplementation("org.openrewrite:rewrite-test:${rewriteVersion}")
    testImplementation("org.assertj:assertj-core:latest.release")
    testImplementation("com.google.guava:guava:29.0-jre")
    testRuntimeOnly("org.openrewrite:rewrite-java-11:${rewriteVersion}")
    testRuntimeOnly("org.openrewrite:rewrite-java-8:${rewriteVersion}")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:latest.release")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    jvmArgs = listOf("-XX:+UnlockDiagnosticVMOptions", "-XX:+ShowHiddenFrames",
            "--add-exports", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
            "--add-exports", "jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
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


publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("java-recipes")
                description.set(project.description)
                inceptionYear.set("2021")
                url.set("https://github.com/breskeby/java-recipes")
                developers {
                    developer {
                        name.set("Rene Groeschke")
                        id.set("breskeby")
                    }
                }
                scm {
                  url.set("https://github.com/breskeby/java-recipes.git")
                }
                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
        }
    }
}


signing {
    sign(publishing.publications["maven"])
}

nexusPublishing {
    repositories {
        sonatype {  //only for users registered in Sonatype after 24 Feb 2021
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}
