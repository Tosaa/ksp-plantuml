dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

pluginManagement {

    plugins {
        kotlin("jvm") version "2.1.21"
        id("com.google.devtools.ksp") version "2.0.20-1.0.25"
    }

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "PumlKSP"

include(":app")
include(":pumlgenerator")
