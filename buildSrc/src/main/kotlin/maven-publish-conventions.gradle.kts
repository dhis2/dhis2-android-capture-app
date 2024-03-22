import java.net.URI

plugins {
    `maven-publish`
}

val VERSION_NAME: String by project
val GROUP: String by project
val POM_NAME: String by project
val POM_ARTIFACT_ID: String by project
val POM_PACKAGING: String by project
val POM_DESCRIPTION: String by project
val POM_URL: String by project
val POM_SCM_URL: String by project
val POM_SCM_CONNECTION: String by project
val POM_SCM_DEV_CONNECTION: String by project
val POM_LICENCE_NAME: String by project
val POM_LICENCE_URL: String by project
val POM_LICENCE_DIST: String by project
val POM_DEVELOPER_ID: String by project
val POM_DEVELOPER_NAME: String by project

fun isReleaseBuild(): Boolean {
    return !VERSION_NAME.contains("SNAPSHOT")
}

val releaseRepositoryUrl: String = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"

val snapshotRepositoryUrl: String = "https://oss.sonatype.org/content/repositories/snapshots/"

fun getRepositoryUsername(): String? {
    return System.getenv("OSSRH_USERNAME")
}

fun getRepositoryPassword(): String? {
    return System.getenv("OSSRH_PASSWORD")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
//            artifact(androidJavadocsJar)

                from(components["release"])

                groupId = GROUP
                artifactId = POM_ARTIFACT_ID
                version = VERSION_NAME

                pom {
                    name.set(POM_NAME)
                    packaging = POM_PACKAGING
                    description.set(POM_DESCRIPTION)
                    url.set(POM_URL)
                    licenses {
                        license {
                            name.set(POM_LICENCE_NAME)
                            url.set(POM_LICENCE_URL)
                            distribution.set(POM_LICENCE_DIST)
                        }
                    }
                    developers {
                        developer {
                            id.set(POM_DEVELOPER_ID)
                            name.set(POM_DEVELOPER_NAME)
                        }
                    }
                    scm {
                        connection.set(POM_SCM_CONNECTION)
                        developerConnection.set(POM_SCM_DEV_CONNECTION)
                        url.set(POM_SCM_URL)
                    }
                }
            }
        }

        repositories {
            maven {
                url =
                    if (isReleaseBuild()) URI(releaseRepositoryUrl) else URI(snapshotRepositoryUrl)

                credentials {
                    username = getRepositoryUsername()
                    password = getRepositoryPassword()
                }
            }
        }
    }
}
