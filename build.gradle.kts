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
        classpath("dev.soffa.foundation:foundation-gradle-plugin:1.0.16")
    }
}


apply(plugin = "foundation.sonatype-publish")

subprojects {
    apply(plugin = "foundation.default-repositories")
}

