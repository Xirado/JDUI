plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
    `maven-publish`
}

group = "at.xirado"
version = "0.1.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.github.freya022:JDA:4b468cdd09")

    api("org.slf4j:slf4j-api:2.0.13")
    api("io.github.oshai:kotlin-logging-jvm:5.1.0")

    api("de.mkammerer.snowflake-id:snowflake-id:0.0.2")
    api("com.github.ben-manes.caffeine:caffeine:3.1.8")

    implementation("org.bouncycastle:bcprov-jdk18on:1.80")
    implementation("de.mkammerer:argon2-jvm:2.12")

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    api("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.8.1")
    api("org.jetbrains.kotlin:kotlin-reflect:2.0.20")

    testImplementation(kotlin("test"))
    testImplementation("org.bouncycastle:bcprov-jdk18on:1.80")
    testImplementation("de.mkammerer:argon2-jvm:2.12")
    testImplementation("io.github.freya022:JDA:4b468cdd09")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

val javadoc: Javadoc by tasks

val sourcesJar = task<Jar>("sourcesJar") {
    from(sourceSets["main"].allSource)
    archiveClassifier.set("sources")
}

val javadocJar = task<Jar>("javadocJar") {
    from(javadoc.destinationDir)
    archiveClassifier.set("javadoc")

    dependsOn(javadoc)
}

tasks {
    build {
        dependsOn(javadocJar)
        dependsOn(sourcesJar)
        dependsOn(jar)
    }
}

publishing {
    repositories {
        maven {
            name = "repoReleases"
            url = uri("https://maven.xirado.dev/releases")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        register<MavenPublication>("release") {
            from(components["java"])
            groupId = "at.xirado"
            artifactId = "JDUI"
            version = project.version as String

            artifact(javadocJar)
            artifact(sourcesJar)
        }
    }
}