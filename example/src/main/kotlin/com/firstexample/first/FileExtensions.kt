package com.firstexample.first


import java.io.File

fun File.writePlantuml(plantumlContent: String): Unit {
    this.writeText("@startuml\n$plantumlContent\n@enduml")
}

val File.isPlantumlDiagram: Boolean
    get() = this.isFile && this.exists() && listOf("@startuml", "@enduml").all { it in this.readText() }

