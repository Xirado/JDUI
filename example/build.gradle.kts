plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlinSerialization)
    application
}

application {
    mainClass.set("at.xirado.jdui.example.MainKt")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":core"))

    implementation(libs.jda)
    implementation("club.minnced:jda-ktx:0.12.0")

    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("org.xerial:sqlite-jdbc:3.45.3.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

    testImplementation(libs.kotlin.test)
}

tasks.test {
    useJUnitPlatform()
}