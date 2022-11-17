plugins {
    id("foundation.java8")
    id("foundation.maven-publish")
    id("foundation.springboot.library")
}

dependencies {
    api(project(":foundation-starter-parent"))

    api("org.springframework.boot:spring-boot-starter-web") {
        exclude(module = "spring-boot-starter-tomcat")
    }
    api("io.reactivex.rxjava3:rxjava:3.1.5")

    implementation("org.springframework.boot:spring-boot-starter-undertow")
    implementation("org.apache.commons:commons-email:1.5")
    implementation("com.sendgrid:sendgrid-java:4.9.3")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework:spring-tx")
    api("org.springframework.cloud:spring-cloud-starter-sleuth")
    implementation("net.logstash.logback:logstash-logback-encoder:7.2")
    api("org.springframework.cloud:spring-cloud-starter-consul-discovery")
    api("com.hazelcast:hazelcast-all:4.2.5")
    implementation("io.sentry:sentry:6.7.0")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.12") {
        exclude(group = "io.github.classgraph")
    }
    implementation("org.springframework.vault:spring-vault-core")

    api("com.amazonaws:aws-java-sdk-s3:1.12.342") {
        exclude(group = "com.github.ben-manes.caffeine")
    }

    implementation("org.springdoc:springdoc-openapi-security:1.6.12") {
        exclude(group = "io.github.classgraph")
    }
    // implementation("io.github.classgraph:classgraph:4.8.147")
    implementation("com.h2database:h2:2.1.214")
    // implementation("net.bytebuddy:byte-buddy:1.12.8")
    testImplementation(project(":foundation-starter-test"))
}
repositories {
    mavenCentral()
}


