import com.google.devtools.ksp.gradle.KspTaskJvm
import org.gradle.kotlin.dsl.support.kotlinCompilerOptions

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
    ksp("tosaa.puml.ksp:pumlgenerator:1.0.0")
}

ksp {

    // Example 1
    /*
    arg("puml.excludedPackages", "com.ignore,com.app.main")
    arg("puml.excludedPropertyNames", "")
    arg("puml.excludedFunctionNames", "<init>,finalize")
    arg("puml.showPublicClasses", "true")
    arg("puml.showPublicProperties", "true")
    arg("puml.showPublicFunctions", "true")
    arg("puml.showInternalClasses", "true")
    arg("puml.showInternalProperties", "true")
    arg("puml.showInternalFunctions", "true")
    arg("puml.showPrivateClasses", "true")
    arg("puml.showPrivateProperties", "true")
    arg("puml.showPrivateFunctions", "true")
    arg("puml.showInheritance", "true")
    arg("puml.showRelations", "true")
    arg("puml.showPackages", "false")
    arg("puml.allowEmptyPackage", "true")
     */

    // Example 2

    arg("puml.title", "Diagram of example 2")
    arg("puml.prefix","""'Here could be anything you'd like to have in your diagram
skinparam class {
    BackgroundColor #fdf0d5
    classFontSize 16
    ArrowColor 003049
    BorderColor 003049
    FontColor 003049
    FontSize 20
}
    """.trimMargin())
    arg("puml.excludedPackages", "com.firstexample")
    arg("puml.excludedPropertyNames", "")
    arg("puml.excludedFunctionNames", "<init>")
    arg("puml.showPublicClasses", "true")
    arg("puml.showPublicProperties", "true")
    arg("puml.showPublicFunctions", "true")
    arg("puml.showInternalClasses", "true")
    arg("puml.showInternalProperties", "true")
    arg("puml.showInternalFunctions", "true")
    arg("puml.showPrivateClasses", "false")
    arg("puml.showPrivateProperties", "false")
    arg("puml.showPrivateFunctions", "false")
    arg("puml.showInheritance", "true")
    arg("puml.showRelations", "true")
    arg("puml.showPackages", "true")
    arg("puml.allowEmptyPackage", "false")

}

application {
    // Define the Fully Qualified Name for the application main class
    // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
    mainClass = "app.AppKt"
}
