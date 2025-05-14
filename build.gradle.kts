import dev.xirado.jdui.configurePublishing

plugins {
    kotlin("jvm") apply true
}

allprojects {
    group = "dev.xirado"
    version = "0.1.0-SNAPSHOT"
}

subprojects {
    afterEvaluate {
        if (name != "example") {
            configurePublishing()
        }
    }
}