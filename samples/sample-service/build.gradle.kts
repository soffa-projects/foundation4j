plugins {
    id("foundation.java8")
    id("foundation.springboot")
    id("foundation.qa.coverage.l6")
}

dependencies {
    implementation(project(":foundation-service"))
    testImplementation(project(":foundation-service-test"))
}
