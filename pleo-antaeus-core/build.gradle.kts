plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation(project(":pleo-antaeus-data"))
    implementation("org.apache.activemq:activemq-broker:5.16.4")
    implementation("org.apache.activemq:activemq-client:5.16.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("dev.inmo:krontab:0.7.1")
    api(project(":pleo-antaeus-models"))

    testImplementation("io.mockk:mockk:1.12.3")
    testImplementation("junit:junit:4.13.2")
}