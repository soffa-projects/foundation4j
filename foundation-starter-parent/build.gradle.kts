plugins {
    id("foundation.java8")
    id("foundation.maven-publish")
    id("foundation.springboot.library")
}

dependencies {
    api("org.springframework.cloud:spring-cloud-starter-consul-discovery")
    api("org.springframework.boot:spring-boot-starter-actuator")
    api("net.logstash.logback:logstash-logback-encoder:7.1.1")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.2")
}
repositories {
    mavenCentral()
}
