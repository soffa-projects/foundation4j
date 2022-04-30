plugins {
    id("foundation.java8")
    id("foundation.maven-publish")
    id("foundation.springboot.library")
}

dependencies {
    api(project(":foundation-core"))
    implementation(project(":foundation-starter-tracing"))
    implementation(project(":foundation-starter-config"))

    api("org.springframework.cloud:spring-cloud-starter-consul-discovery")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("net.logstash.logback:logstash-logback-encoder:7.1.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.2")
}
repositories {
    mavenCentral()
}
