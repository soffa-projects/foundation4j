plugins {
    idea
}

ext["caffeine.version"] = "2.9.3"

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            setUrl("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("dev.soffa.foundation:foundation-gradle-plugin:1.0.2")
    }
}


apply(plugin = "foundation.sonatype-legacy-publish")

allprojects {
    apply(plugin = "foundation.default-repositories")
    apply(plugin = "foundation.java8")
}

tasks.withType<Test>().configureEach {
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
    setForkEvery(100)
    reports.html.required.set(false)
    reports.junitXml.required.set(false)
}

tasks.withType<JavaCompile>().configureEach {
    options.isFork = true
}
