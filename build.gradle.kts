plugins {
    kotlin("jvm") version "1.9.0"
    `maven-publish`
    id("signing")

}

group = "net.codetreats"
version = "3.0.0"

repositories {
    mavenCentral()
}

dependencies {
    api("io.ktor:ktor-client-core:3.0.1")
    api("io.ktor:ktor-client-cio:3.0.1")
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