plugins {
    id("foundation.java8")
    id("foundation.maven-publish")
    id("foundation.springboot.library")
}

dependencies {
    api(project(":foundation-api"))
    api(project(":foundation-core"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa") {
        exclude(group = "com.zaxxer")
        exclude(group = "com.github.ben-manes.caffeine")
    }
    implementation("com.github.ben-manes.caffeine:caffeine") {  // Don't use version 3, it's not compatible with Java8
        version {
            strictly("2.9.3")
        }
    }
    implementation("org.jobrunr:jobrunr:5.1.3") {
        exclude(group = "com.zaxxer")
    }
    implementation("org.questdb:questdb:6.4.1-jdk8")
    implementation("com.influxdb:influxdb-client-java:6.1.0")

    implementation("org.postgresql:postgresql:42.3.6")
    @Suppress("GradlePackageUpdate")
    // HikariCP 5+ is not compatible with Java8
    implementation("com.zaxxer:HikariCP:4.0.3") {
        exclude(group = "com.github.ben-manes.caffeine")
    }
    implementation("io.pebbletemplates:pebble:3.1.5")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:4.36.0")
    implementation("net.javacrumbs.shedlock:shedlock-spring:4.36.0")
    api("org.hibernate.javax.persistence:hibernate-jpa-2.1-api:1.0.2.Final")
    implementation("org.jdbi:jdbi3-core:3.30.0") {
        exclude(group = "com.github.ben-manes.caffeine")
    }
    implementation("org.liquibase:liquibase-core:4.11.0")
    implementation("org.jdbi:jdbi3-postgres:3.30.0")
    implementation("org.jdbi:jdbi3-sqlobject:3.30.0")
    testImplementation(project(":foundation-starter"))
    testImplementation(project(":foundation-starter-test"))
}
repositories {
    mavenCentral()
}

