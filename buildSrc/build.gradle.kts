repositories {
    google()
    mavenCentral()
}

plugins {
    kotlin("jvm") version "2.1.0" // 👈 use the correct version
    `kotlin-dsl` apply true
}

dependencies {
    gradleApi()
    implementation(libs.jvm)
    implementation(libs.publishing)
}