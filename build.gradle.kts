plugins {
    id("kanbando.jvm-library")
    id("kanbando.publish")
}

group = "io.kanbando"
version = "0.1.0-SNAPSHOT"

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
}
