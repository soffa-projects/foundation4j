plugins {
    id("foundation.java8")
    id("foundation.maven-publish")
    id("foundation.test.junit5")
    id("foundation.qa.coverage.l1")
}

dependencies {
    api(project(":foundation-api"))

    api("commons-beanutils:commons-beanutils:1.9.4")
    // api("io.github.openfeign:feign-core:11.8")
    api("joda-time:joda-time:2.10.13")
    api("com.github.michaelgantman:MgntUtils:1.6.0.1")
    api("com.joestelmach:natty:0.13")
    api("org.json:json:20211205")
    api("com.jayway.jsonpath:json-path:2.7.0")
    api("org.apache.commons:commons-text:1.9")

    api("org.glassfish:javax.el:3.0.0")
    api("commons-io:commons-io:2.11.0")
    api("com.squareup.okhttp3:okhttp:4.9.3")
    api("com.konghq:unirest-java:3.13.6")
    api("com.google.guava:guava:31.1-jre")
    @Suppress("GradlePackageUpdate")
    implementation("commons-codec:commons-codec:1.15")
    implementation("io.pebbletemplates:pebble:3.1.5")
    implementation("com.auth0:java-jwt:3.18.3")
    implementation("com.nimbusds:nimbus-jose-jwt:9.21")
    implementation("com.aventrix.jnanoid:jnanoid:2.0.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${property("jackson.version")}")

    implementation("com.jsoniter:jsoniter:0.9.23") {
        exclude(group = "com.fasterxml.jackson.core")
        exclude(group = "com.google.code.gson")
    }

    implementation("com.github.michaelgantman:MgntUtils:1.6.0.1")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${property("jackson.version")}")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${property("jackson.version")}")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:${property("jackson.version")}")

}
repositories {
    mavenCentral()
}

