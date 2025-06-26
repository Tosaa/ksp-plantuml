import com.google.devtools.ksp.gradle.KspTaskJvm
import org.gradle.kotlin.dsl.support.kotlinCompilerOptions
import java.io.File

plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    kotlin("jvm")
    id("com.google.devtools.ksp") version "2.1.21-2.0.1"

    // Apply the Application plugin to add support for building an executable JVM application.
    application

}
val firstExampleConfiguration = mutableMapOf<String, String>().apply {
    put("puml.excludedPackages", "com.ignore,com.app.main")
    put("puml.excludedPropertyNames", "")
    put("puml.excludedFunctionNames", "<init>,finalize")
    put("puml.showPublicClasses", "true")
    put("puml.showPublicProperties", "true")
    put("puml.showPublicFunctions", "true")
    put("puml.showInternalClasses", "true")
    put("puml.showInternalProperties", "true")
    put("puml.showInternalFunctions", "true")
    put("puml.showPrivateClasses", "true")
    put("puml.showPrivateProperties", "true")
    put("puml.showPrivateFunctions", "true")
    put("puml.showInheritance", "true")
    put("puml.showPropertyRelations", "true")
    put("puml.showFunctionRelations", "false")
    put("puml.showPackages", "false")
    put("puml.allowEmptyPackage", "true")
}

tasks {
    register("kspExample1") {
        ksp {
            firstExampleConfiguration.forEach {
                arg(it.key, it.value)
            }
        }
        dependsOn(findByName("kspKotlin"))
    }
}

val secondExampleConfiguration = mutableMapOf<String, String>().apply {
    put("puml.title", "Diagram of example 2")
    put(
        "puml.prefix", """'Here could be anything you'd like to have in your diagram
skinparam class {
    BackgroundColor #fdf0d5
    classFontSize 16
    ArrowColor 003049
    BorderColor 003049
    FontColor 003049
    FontSize 20
}
    """.trimMargin()
    )
    put("puml.excludedPackages", "com.firstexample")
    put("puml.excludedPropertyNames", "")
    put("puml.excludedFunctionNames", "<init>,equals,hashCode,toString")
    put("puml.showVisibilityModifiers", "false")
    put("puml.showPublicClasses", "true")
    put("puml.showPublicProperties", "true")
    put("puml.showPublicFunctions", "true")
    put("puml.showInternalClasses", "true")
    put("puml.showInternalProperties", "true")
    put("puml.showInternalFunctions", "true")
    put("puml.showPrivateClasses", "false")
    put("puml.showPrivateProperties", "false")
    put("puml.showPrivateFunctions", "false")
    put("puml.showInheritance", "true")
    put("puml.showPropertyRelations", "true")
    put("puml.showFunctionRelations", "false")
    put("puml.showPackages", "true")
    put("puml.allowEmptyPackage", "false")
}

tasks {
    register("kspExample2") {
        ksp {
            secondExampleConfiguration.forEach {
                arg(it.key, it.value)
            }
        }
        dependsOn(findByName("kspKotlin"))
    }
}

dependencies {
    // Project "app" depends on project "utils". (Project paths are separated with ":", so ":utils" refers to the top-level "utils" project.)
    ksp("io.github.tosaa.puml.ksp:ksp-plantuml-generator:0.0.1")
}

application {
    // Define the Fully Qualified Name for the application main class
    // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
    mainClass = "app.AppKt"
}
