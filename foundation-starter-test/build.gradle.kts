plugins {
    id("foundation.java8")
    id("foundation.springboot.library")
    id("foundation.maven-publish")
}

dependencies {
    compileOnly(project(":foundation-core"))
    api(project(":foundation-commons"))

    api("com.intuit.karate:karate-junit5:1.2.0")
    api("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "com.vaadin.external.google")
    }
    implementation("com.hazelcast:hazelcast-all:4.2.5")

    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-starter-security")

    @Suppress("GradlePackageUpdate")
    api("com.h2database:h2:2.1.210")
    api("com.google.guava:guava:31.1-jre")
    @Suppress("GradlePackageUpdate")
    api("commons-io:commons-io:2.8.0")
    api("org.awaitility:awaitility:4.2.0")
    api("com.github.javafaker:javafaker:1.0.2") {
        exclude(module = "snakeyaml")
    }

    testImplementation("org.springframework.boot:spring-boot-starter-web:2.7.0")
}
repositories {
    mavenCentral()
}
