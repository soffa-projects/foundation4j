plugins {
    id("foundation.java8")
    id("foundation.maven-publish")
    id("foundation.springboot.library")
}

dependencies {
    implementation(platform("io.opentelemetry:opentelemetry-bom:1.13.0"))
    implementation("io.opentelemetry:opentelemetry-api")
}

