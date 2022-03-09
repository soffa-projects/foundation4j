plugins {
    id("foundation.java8")
    id("foundation.maven-publish")
    id("foundation.test.junit5")
    id("foundation.qa.coverage.l3")
}

dependencies {
    api(project(":foundation-annotations"))
    api("javax.validation:validation-api:2.0.1.Final")
    api("org.checkerframework:checker-qual:3.21.2")
    api("org.checkerframework:checker:3.21.1")
    api("org.checkerframework:jdk8:3.3.0")
    api("javax.annotation:javax.annotation-api:1.3.2")
    api("io.swagger.core.v3:swagger-annotations:2.1.13")
    api("io.swagger.core.v3:swagger-models:2.1.13")
    api("javax.ws.rs:javax.ws.rs-api:2.1.1")
    api("com.fasterxml.jackson.core:jackson-annotations:2.13.1")
    api("com.fasterxml.jackson.core:jackson-databind:2.13.1")
    api("com.google.code.findbugs:jsr305:3.0.2")
    api("org.hamcrest:hamcrest-core:2.2")
    compileOnly("com.google.code.gson:gson:2.9.0")
    compileOnly("com.intuit.karate:karate-junit5:1.2.0.RC4")

}

