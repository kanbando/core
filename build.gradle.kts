plugins {
    id("kanbando.kmp-shared-library")
    id("kanbando.publish")
}

group = "io.kanbando"
version = "0.1.0-SNAPSHOT"

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
