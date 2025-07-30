import dev.xirado.jdui.configurePublishing

plugins {
    kotlin("jvm") apply true
}

allprojects {
    group = "dev.xirado"
    version = "0.2.1"
}

val toPublish = listOf("core")

subprojects {
    if (name in toPublish) {
        configurePublishing()
    }
}