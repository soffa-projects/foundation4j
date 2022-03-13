plugins {
    id("foundation.java8")
    id("foundation.maven-publish")
    id("foundation.springboot.library")
}

dependencies {
    api(project(":foundation-core"))

    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(module = "spring-boot-starter-tomcat")
    }
    runtimeOnly("org.springframework.boot:spring-boot-starter-undertow")
    implementation("org.apache.commons:commons-email:1.5")
    implementation("com.sendgrid:sendgrid-java:4.8.3")
    implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework:spring-tx")
    implementation("org.springframework.cloud:spring-cloud-starter-sleuth")
    implementation("net.logstash.logback:logstash-logback-encoder:7.0.1")
    implementation("org.springframework.cloud:spring-cloud-starter-vault-config")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.6") {
        exclude(group = "io.github.classgraph")
    }
    implementation("org.springdoc:springdoc-openapi-kotlin:1.6.6")
    implementation("org.springdoc:springdoc-openapi-security:1.6.6") {
        exclude(group = "io.github.classgraph")
    }
    implementation("org.reflections:reflections:0.10.2")
    implementation("io.github.classgraph:classgraph:4.8.140")
    implementation("com.h2database:h2:2.1.210")
    implementation("net.bytebuddy:byte-buddy:1.12.8")
    testImplementation(project(":foundation-starter-test"))
}
repositories {
    mavenCentral()
}


