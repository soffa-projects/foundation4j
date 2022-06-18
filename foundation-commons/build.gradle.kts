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
    api("joda-time:joda-time:2.10.14")
    api("com.github.michaelgantman:MgntUtils:1.6.0.3")
    api("com.joestelmach:natty:0.13")
    api("org.json:json:20220320")
    api("com.jayway.jsonpath:json-path:2.7.0")
    api("org.apache.commons:commons-text:1.9")
    implementation("com.github.f4b6a3:uuid-creator:4.6.1")

    api("org.glassfish:javax.el:3.0.0")
    api("commons-io:commons-io:2.11.0")
    api("com.squareup.okhttp3:okhttp:4.10.0")
    api("com.konghq:unirest-java:3.13.10")
    api("com.google.guava:guava:31.1-jre")
    @Suppress("GradlePackageUpdate")
    implementation("io.github.classgraph:classgraph:4.8.147")
    implementation("commons-codec:commons-codec:1.15")
    implementation("io.pebbletemplates:pebble:3.1.5")
    implementation("com.auth0:java-jwt:3.19.2")
    implementation("com.nimbusds:nimbus-jose-jwt:9.23")
    implementation("com.aventrix.jnanoid:jnanoid:2.0.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${property("jackson.version")}")
    implementation("com.github.michaelgantman:MgntUtils:1.6.0.3")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${property("jackson.version")}") 
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${property("jackson.version")}")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:${property("jackson.version")}")

}
