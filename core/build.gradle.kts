plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlinSerialization)
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compileOnly(libs.jda)

    api(libs.slf4j)
    api(libs.kotlin.logging)

    api(libs.snowflake)
    api(libs.caffeine)

    api(libs.kotlin.coroutines)
    api(libs.kotlin.protobuf)
    api(libs.kotlin.reflect)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.jda)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}