plugins {
    id("foundation.java8")
    id("foundation.springboot")
    id("foundation.qa.coverage.l2")
}

dependencies {
    implementation(project(":foundation-starter"))
    implementation(project(":foundation-starter-data"))
    testImplementation(project(":foundation-starter-test"))
}
