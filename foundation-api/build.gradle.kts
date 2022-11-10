plugins {
    // id("foundation.java8")
    id("foundation.kotlin")
    id("foundation.maven-publish")
    id("foundation.test.junit5")
    id("foundation.qa.coverage.l2")
}

dependencies {
    api(project(":foundation-annotations"))
    api("javax.validation:validation-api:2.0.1.Final")
    api("org.checkerframework:checker-qual:3.25.0")
    api("org.checkerframework:checker:3.25.0")
    api("org.checkerframework:jdk8:3.3.0")
    api("javax.annotation:javax.annotation-api:1.3.2")
    api("io.swagger.core.v3:swagger-annotations:2.2.6")
    api("io.swagger.core.v3:swagger-models:2.2.6")
    api("javax.transaction:javax.transaction-api:1.3")
    api("org.springdoc:springdoc-openapi-common:1.6.12")
    api("javax.ws.rs:javax.ws.rs-api:2.1.1")
    api("com.fasterxml.jackson.core:jackson-annotations:${property("jackson.version")}")
    api("com.fasterxml.jackson.core:jackson-databind:${property("jackson.version")}")
    api("com.google.code.findbugs:jsr305:3.0.2")
    api("org.hamcrest:hamcrest-core:2.2")
    compileOnly("com.google.code.gson:gson:2.10")
    compileOnly("com.intuit.karate:karate-junit5:1.2.0")
    api(platform("io.opentelemetry:opentelemetry-bom:1.19.0"))
    api("io.opentelemetry:opentelemetry-api")


}
repositories {
    mavenCentral()
}

