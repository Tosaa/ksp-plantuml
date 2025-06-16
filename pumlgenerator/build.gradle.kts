import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("com.vanniktech.maven.publish") version "0.30.0"

}

group = "io.github.tosaa.puml.ksp"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:2.1.21-2.0.1")
    testImplementation(kotlin("test"))
    testImplementation("dev.zacsweers.kctfork:ksp:0.7.1")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}


mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates(group.toString(), "ksp-plantuml-generator", version.toString())

    pom {
        name = "ksp-plantuml-generator"
        description = "A Kotlin Symbol processor for Plantuml diagram generation."
        inceptionYear = "2025"
        url = "https://github.com/tosaa/ksp-plantuml/"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "tosaa-ksp-plantuml"
                name = "Tosaa"
                url = "https://github.com/tosaa/"
            }
        }
        scm {
            url = "https://github.com/tosaa/ksp-plantuml/"
            connection = "scm:git:git://github.com/tosaa/ksp-plantuml.git"
            developerConnection = "scm:git:ssh://git@github.com/tosaa/ksp-plantuml.git"
        }
    }
}

/*
publishing {



    publications.create<MavenPublication>("ksp-puml-plugin").from(components["kotlin"])

    repositories.mavenLocal()
}
*/