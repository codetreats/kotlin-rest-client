plugins {
    kotlin("jvm") version "1.9.0"
    `maven-publish`
}

group = "net.codetreats"
version = "0.0.1-SNAPSHOT"

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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = project.group as String
            artifactId = "kotlin-rest-client"
            version = project.version as String
        }
    }

    repositories {
        maven {
            name = "MavenCentral"
            if (project.version.toString().endsWith("-SNAPSHOT")) {
                setUrl("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            } else {
                setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            }


            credentials {
                username = project.findProperty("mavenCentralUsername")!!.toString()
                password = project.findProperty("mavenCentralPassword")!!.toString()
            }
        }
    }
}