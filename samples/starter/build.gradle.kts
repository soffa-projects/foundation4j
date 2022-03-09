plugins {
    id("foundation.java8")
    id("foundation.springboot")
    id("foundation.qa.coverage.l6")
}

dependencies {
    implementation(project(":foundation-starter"))
    testImplementation(project(":foundation-starter-test"))
}
