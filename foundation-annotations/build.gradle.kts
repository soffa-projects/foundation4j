plugins {
    id("foundation.java8")
    id("foundation.maven-publish")
}

dependencies {
    compileOnly("org.springframework:spring-web:5.3.23")
    compileOnly("javax.inject:javax.inject:1")
}

repositories {
    mavenCentral()

}

