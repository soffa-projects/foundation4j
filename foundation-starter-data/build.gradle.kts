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
    implementation("org.postgresql:postgresql:42.3.3")
    @Suppress("GradlePackageUpdate")
    implementation("com.zaxxer:HikariCP:4.0.3") {
        exclude(group = "com.github.ben-manes.caffeine")
    }
    implementation("io.pebbletemplates:pebble:3.1.5")
    implementation("org.reflections:reflections:0.10.2")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:4.33.0")
    implementation("net.javacrumbs.shedlock:shedlock-spring:4.33.0")
    api("org.hibernate.javax.persistence:hibernate-jpa-2.1-api:1.0.2.Final")
    implementation("org.jdbi:jdbi3-core:3.28.0") {
        exclude(group = "com.github.ben-manes.caffeine")
    }
    implementation("org.liquibase:liquibase-core:4.9.1")
    implementation("org.jdbi:jdbi3-postgres:3.28.0")
    implementation("org.jdbi:jdbi3-sqlobject:3.28.0")

    testImplementation(project(":foundation-starter"))
    testImplementation(project(":foundation-starter-test"))
}
repositories {
    mavenCentral()
}


/*
api("org.jobrunr:jobrunr:4.0.7") {
    exclude(group = "com.zaxxer")
    exclude(group = "com.h2database")
}

 */

