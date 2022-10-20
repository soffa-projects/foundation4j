plugins {
    //id("foundation.java8")
    id("foundation.kotlin")
    id("foundation.maven-publish")
    id("foundation.test.junit5")
    //id("foundation.qa.coverage.l1")
}

dependencies {
    api(project(":foundation-api"))
    api(project(":foundation-commons"))
    api("javax.inject:javax.inject:1")
    api("javax.transaction:javax.transaction-api:1.3")
    implementation("com.auth0:java-jwt:4.1.0")
    implementation("com.nimbusds:nimbus-jose-jwt:9.25.6")
    implementation("commons-validator:commons-validator:1.7")
    api("io.reactivex.rxjava3:rxjava:3.1.5")
    implementation("com.amazonaws:aws-java-sdk-s3:1.12.322")
    @Suppress("GradlePackageUpdate")
    api("com.github.ben-manes.caffeine:caffeine") {  // Don't use version 3, it's not compatible with Java8
        version {
            strictly("2.9.3")
        }
    }
    @Suppress("GradlePackageUpdate")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure:2.6.3")

}
repositories {
    mavenCentral()
}

// api("com.jayway.jsonpath:json-path:2.7.0")
