plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    kotlin("jvm")
    id("com.google.devtools.ksp") version "2.1.21-2.0.1"

    // Apply the Application plugin to add support for building an executable JVM application.
    application

}

dependencies {
    // Project "app" depends on project "utils". (Project paths are separated with ":", so ":utils" refers to the top-level "utils" project.)
    implementation(project(":pumlgenerator"))
    ksp(project(":pumlgenerator"))
}

ksp{
    arg("wl", "com")
}

application {
    // Define the Fully Qualified Name for the application main class
    // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
    mainClass = "app.AppKt"
}
