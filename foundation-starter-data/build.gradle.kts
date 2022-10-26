plugins {
    id("foundation.java8")
    id("foundation.maven-publish")
    id("foundation.springboot.library")
}

dependencies {
    api(project(":foundation-api"))
    api(project(":foundation-core"))

    api("org.springframework.boot:spring-boot-starter-data-jpa") {
        exclude(group = "com.zaxxer")
        exclude(group = "com.github.ben-manes.caffeine")
    }
    implementation("com.github.ben-manes.caffeine:caffeine") {  // Don't use version 3, it's not compatible with Java8
        version {
            strictly("2.9.3")
        }
    }
    implementation("org.jobrunr:jobrunr:5.3.0") {
        exclude(group = "com.zaxxer")
    }
    implementation("org.questdb:questdb:6.5.4-jdk8")
    implementation("com.influxdb:influxdb-client-java:6.6.0")

    implementation("org.postgresql:postgresql:42.5.0")
    @Suppress("GradlePackageUpdate")
    // HikariCP 5+ is not compatible with Java8
    implementation("com.zaxxer:HikariCP:4.0.3") {
        exclude(group = "com.github.ben-manes.caffeine")
    }
    // implementation("io.pebbletemplates:pebble:3.1.6")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:4.42.0")
    implementation("net.javacrumbs.shedlock:shedlock-spring:4.42.0")
    api("org.hibernate.javax.persistence:hibernate-jpa-2.1-api:1.0.2.Final")
    implementation("org.jdbi:jdbi3-core:3.34.0") {
        exclude(group = "com.github.ben-manes.caffeine")
    }
    implementation("com.healthmarketscience.sqlbuilder:sqlbuilder:3.0.2")
    implementation("org.liquibase:liquibase-core:4.17.1")
    implementation("org.jdbi:jdbi3-postgres:3.34.0")
    implementation("org.jdbi:jdbi3-sqlobject:3.34.0")
    testImplementation(project(":foundation-starter"))
    testImplementation(project(":foundation-starter-test"))

    implementation(platform("org.testcontainers:testcontainers-bom:1.17.4"))
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")

}
repositories {
    mavenCentral()
}

