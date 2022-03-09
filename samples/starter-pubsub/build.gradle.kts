plugins {
    id("foundation.java8")
    id("foundation.springboot")
    id("foundation.qa.coverage.l1")
}

dependencies {
    implementation(project(":foundation-starter"))
    implementation(project(":foundation-starter-pubsub"))
    testImplementation(project(":foundation-starter-test"))
}
