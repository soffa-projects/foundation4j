plugins {
    id("foundation.java8")
    id("foundation.maven-publish")
    id("foundation.springboot.library")
}

dependencies {
    api(project(":foundation-starter-parent"))

    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(module = "spring-boot-starter-tomcat")
    }
    runtimeOnly("org.springframework.boot:spring-boot-starter-undertow")
    implementation("org.apache.commons:commons-email:1.5")
    implementation("com.sendgrid:sendgrid-java:4.9.1")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework:spring-tx")
    implementation("net.logstash.logback:logstash-logback-encoder:7.1.1")
    implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")
    implementation("org.jobrunr:jobrunr:5.1.3") {
        exclude(group = "com.zaxxer")
    }
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.8") {
        exclude(group = "io.github.classgraph")
    }
    implementation("org.springdoc:springdoc-openapi-kotlin:1.6.8")
    implementation("org.springdoc:springdoc-openapi-security:1.6.8") {
        exclude(group = "io.github.classgraph")
    }
    implementation("io.github.classgraph:classgraph:4.8.146")
    implementation("com.h2database:h2:2.1.212")
    // implementation("net.bytebuddy:byte-buddy:1.12.8")
    testImplementation(project(":foundation-starter-test"))
}
repositories {
    mavenCentral()
}


