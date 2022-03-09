plugins {
    id("foundation.java8")
    id("foundation.maven-publish")
    id("foundation.springboot.library")
}


dependencies {
    implementation(platform("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:latest.release"))
    implementation("com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter")
}
