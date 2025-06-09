plugins {
    kotlin("jvm")
    id("maven-publish")
}

group = "tosaa.puml.ksp"
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

publishing {
    publications.create<MavenPublication>("ksp-puml-plugin").from(components["kotlin"])

    repositories.mavenLocal()
}
