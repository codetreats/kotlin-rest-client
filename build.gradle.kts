plugins {
    kotlin("jvm") version "2.2.21"
    `maven-publish`
    id("signing")
    id("com.diffplug.spotless") version "8.2.1"
}

group = "net.codetreats"
version = "3.0.0"

repositories {
    mavenCentral()
}

dependencies {
    val ktor = "3.4.0"
    api("io.ktor:ktor-client-core:$ktor")
    api("io.ktor:ktor-client-cio:$ktor")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
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
        maven {
            this.name = "repo"
            this.url = uri(project.findProperty("repo.url")!!.toString())
            this.isAllowInsecureProtocol = true
        }
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

spotless {
    kotlin {
        target("src/**/*.kt")
        ktlint("1.0.1")
            .editorConfigOverride(
                mapOf(
                    "indent_size" to "4",
                    "max_line_length" to "120",
                    "ktlint_standard_no-wildcard-imports" to "disabled",
                ),
            )
        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlinGradle {
        target("*.gradle.kts")
        ktlint("1.0.1")
        trimTrailingWhitespace()
        endWithNewline()
    }
}
