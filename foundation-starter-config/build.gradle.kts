plugins {
    id("foundation.java8")
    id("foundation.maven-publish")
    id("foundation.springboot.library")
}

dependencies {
    api(project(":foundation-api"))
    api(project(":foundation-core"))
    implementation("org.springframework.cloud:spring-cloud-starter-vault-config")
}

