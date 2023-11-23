plugins {
    kotlin("jvm") version "1.9.0"
    `maven-publish`
    id("signing")

}

group = "net.codetreats"
version = "0.0.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-core:2.3.4")
    implementation("io.ktor:ktor-client-cio:2.3.4")
    implementation("io.ktor:ktor-client-logging:2.3.4")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

signing {
    sign(publishing.publications)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = "net.codetreats"
            artifactId = "kotlin-rest-client"
            version = project.version as String

            pom {
                name = "kotlin-rest-client"
                packaging = "jar"
                description = "a simple kotlin rest client"
                url = "https://github.com/codetreats/kotlin-rest-client"
                licenses {
                    license {
                        name = "MIT License"
                        url = "https://opensource.org/licenses/MIT"
                    }
                }
                developers {
                    developer {
                        id = "martin"
                        name = "Martin"
                        email = "mail@codetreats.net"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/codetreats/kotlin-rest-client.git"
                    developerConnection = "scm:git:git://github.com/codetreats/kotlin-rest-client.git"
                    url = "https://github.com/codetreats/kotlin-rest-client.git"
                }
            }
        }
    }

    repositories {
        if (project.version.toString().endsWith("-SNAPSHOT")) {
            configureRepo("snapshot", false)
        } else {
            configureRepo("release", true)
        }
    }
}

fun RepositoryHandler.configureRepo(type: String, secureProtocol: Boolean) {
    maven {
        this.name = type
        this.url = uri(project.findProperty("$type.repo.url")!!.toString())
        this.isAllowInsecureProtocol = !secureProtocol
        credentials {
            this.username = project.findProperty("$type.repo.user")!!.toString()
            this.password = project.findProperty("$type.repo.password")!!.toString()
        }
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}