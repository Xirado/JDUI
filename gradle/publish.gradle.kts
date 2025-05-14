//import com.vanniktech.maven.publish.MavenPublishBaseExtension
//plugins {
//    id("com.vanniktech.maven.publish")
//    signing
//}


apply(plugin = "com.vanniktech.maven.publish")
apply(plugin = "signing")

buildscript {
    val mavenCentralUsername: String? by project
    val mavenCentralPassword: String? by project

    val pgpKeyId: String? by project
    val pgpSecretKey: String? by project

    val canSign = pgpKeyId != null && pgpSecretKey != null
    val isSnapshot = (version as String).endsWith("-SNAPSHOT")
    val canPublish = mavenCentralUsername != null && mavenCentralPassword != null && (canSign || isSnapshot)



    if (canPublish) {
        configure<SigningExtension> {
            isRequired = !isSnapshot
            useInMemoryPgpKeys(pgpKeyId, pgpSecretKey, "")
        }

        configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
            signAllPublications()
            coordinates("dev.xirado", "jdui-${project.name}", project.version as String)

            pom {
                description = "Kotlin library for effortlessly creating stateful and interactive messages in Discord with 0 boilerplate"
                url = "https://github.com/Xirado/JDUI"

                licenses {
                    license {
                        name = "Apache License, Version 2.0"
                        url = "https://opensource.org/license/apache-2-0"
                        distribution = "repo"
                    }
                }

                developers {
                    developer {
                        id.set("Xirado")
                        name.set("Marcel Korzonek")
                        email.set("marcel@xirado.dev")
                    }
                }

                scm {
                    connection = "scm:git:https://github.com/Xirado/JDUI.git"
                    developerConnection = "scm:git:https://github.com/Xirado/JDUI.git"
                    url = "https://github.com/Xirado/JDUI"
                }
            }
        }
    }
}