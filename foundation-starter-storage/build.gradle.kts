plugins {
    id("foundation.java8")
    id("foundation.maven-publish")
    id("foundation.springboot.library")
    id("foundation.qa.coverage.l3")
}

dependencies {
    api(project(":foundation-commons"))
    api("com.amazonaws:aws-java-sdk-s3:1.12.239") {
        exclude(group = "com.github.ben-manes.caffeine")
    }

    implementation("com.github.ben-manes.caffeine:caffeine:2.9.3") // Don't use version 3, it's not compatible with Java8
    compileOnly("org.springframework.boot:spring-boot-autoconfigure:2.7.0")
    testImplementation(project(":foundation-starter-test"))
}
repositories {
    mavenCentral()
}

