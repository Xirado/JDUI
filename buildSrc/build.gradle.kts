repositories {
    google()
    mavenCentral()
}

plugins {
    alias(libs.plugins.kotlinJvm)
    `kotlin-dsl` apply true
}

dependencies {
    gradleApi()
    implementation(libs.jvm)
    implementation(libs.publishing)
}