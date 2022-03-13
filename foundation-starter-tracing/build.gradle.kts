plugins {
    id("foundation.java8")
    id("foundation.maven-publish")
    id("foundation.springboot.library")
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-zipkin:2.2.8.RELEASE")
    implementation("org.springframework.cloud:spring-cloud-starter-sleuth")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(project(":foundation-starter"))
}


