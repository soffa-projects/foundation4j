plugins {
    id("foundation.java8")
    id("foundation.maven-publish")
    id("foundation.springboot.library")
    id("foundation.qa.coverage.l4")
}

dependencies {
    api(project(":foundation-core"))

    implementation("io.nats:jnats:2.14.0")
    implementation("com.github.fridujo:rabbitmq-mock:1.1.1")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.amqp:spring-rabbit")
    //implementation("org.apache.kafka:kafka_2.13:3.1.0")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    testImplementation(project(":foundation-starter"))
    testImplementation(project(":foundation-starter-test"))
    testImplementation("berlin.yuna:nats-server-embedded:2.2.89")

}
repositories {
    mavenCentral()
}

