dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

pluginManagement {

    plugins {
        kotlin("jvm") version "2.2.21"
        id("com.google.devtools.ksp") version "2.3.3"
    }

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

}

rootProject.name = "PumlKSP"

include(":example")
include(":pumlgenerator")
