plugins {
    id("foundation.java8")
    id("foundation.maven-publish")
    id("foundation.springboot.library")
}

dependencies {
    api(project(":foundation-core"))
    api(project(":foundation-starter"))
}
