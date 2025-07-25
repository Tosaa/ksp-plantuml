package docgeneration

import CompilationTest
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test

const val GENERATION_REFERENCE_COMMENT = "'This file is generated by the GenerateExamplesForDoc test."

class GenerationForDoc : CompilationTest() {

    val dataClassCode = """
    package my.pck
    data class Foo(val x: String, val y: String, val z : Int)
    """

    val interfaceCode = """
    package com.google.devtools.ksp.processing
    import com.google.devtools.ksp.symbol.KSAnnotated
    interface SymbolProcessor {
        fun process(resolver: Resolver): List<KSAnnotated>
        fun finish()
        fun onError()
    }
    """

    val extensionFunctionsCode = """
    package my.pck
    class Color(val hex:String) {
        companion object
    }
    
    fun Color.halfTransparent() : Color = Color("7f"+this.hex.takeLast(6))
    val Color.Companion.red : Color
        get() = Color("ffff0000")
    fun Color.Companion.byRGBInt(alpha : Int, red : Int, green : Int, blue : Int) : Color = Color(alpha.toString(16)+red.toString(16)+green.toString(16)+blue.toString(16))
    """

    val objectCode = """
    package my.logging
    object Logger {
        fun warn(message:String){}
        fun warn(message:()->String) = warn(message()) 
        fun info(message:String){}
        fun info(message:()->String) = info(message()) 
        fun verbose(message:String){}
        fun verbose(message:()->String) = verbose(message()) 
        fun error(message:String, throwable:Throwable?=null){}
        fun error(message:()->String, throwable:Throwable?=null) = error(message(),throwable)
    }
    """

    val companionObjectCode = """
    package user
    class User private constructor(val name: String) {
        // Defines a companion object that acts as a factory for creating User instances
        companion object Factory {
            fun create(name: String): User = User(name)
            fun createOnlyIfValid(name:String): User? = name.takeIf{ it.isNotEmpty() }?.let{ User(name) }
        }
    }  
    """

    val enumsCode = """
    package trafficLight
    enum class TrafficLights(val description:String) {
        RED("You better stay"),
        YELLOW("If you think about it, better stand still"),
        GREEN("Lets go!");
        fun canCrossSafely():Boolean = when(this){
            GREEN -> true
            RED, YELLOW -> false
        }
    }  
    """

    val inheritanceCode = """
    // Example inspired from https://en.wikipedia.org/wiki/Factory_method_pattern
    interface Room {
        fun connect(room: Room?)
    }
    
    class MagicRoom : Room {
        override fun connect(room: Room?) {}
    }
    
    class OrdinaryRoom : Room {
        override fun connect(room: Room?) {}
    }
    
    abstract class MazeGame {
        val rooms: MutableList<Room> = ArrayList()
    
        init {
            val room1 = makeRoom()
            val room2 = makeRoom()
            room1.connect(room2)
            rooms.add(room1)
            rooms.add(room2)
        }
    
        protected abstract fun makeRoom(): Room
    }
    class MagicMazeGame : MazeGame() {
        override fun makeRoom(): MagicRoom {
            return MagicRoom()
        }
    }
    
    class OrdinaryMazeGame : MazeGame() {
        override fun makeRoom(): OrdinaryRoom {
            return OrdinaryRoom()
        }
    }  
    """

    val suspendCode = """
    package events
    data class Event(val id:Int)
    interface EventsRepository {
        val events : List<Event>
        suspend fun fetchEvents() : Result<Unit>
    }
    """

    val sealedClassesCode = """
    package ui
    sealed class UIState {
        data object Loading : UIState()
        data class Success(val data: String) : UIState()
        data class Error(val exception: Exception) : UIState()
    }
    """

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `generate Docs`() {
        listOf(
            Triple("DataClass", dataClassCode, DEFAULT_OPTIONS),
            Triple("Interface", interfaceCode, DEFAULT_OPTIONS),
            Triple("Extensions", extensionFunctionsCode, DEFAULT_OPTIONS),
            Triple("Objects", objectCode, DEFAULT_OPTIONS),
            Triple("CompanionObjects", companionObjectCode, DEFAULT_OPTIONS),
            Triple("Enums", enumsCode, DEFAULT_OPTIONS),
            Triple("Inheritance", inheritanceCode, DEFAULT_OPTIONS),
            Triple("InheritanceWithInheritedFields", inheritanceCode, DEFAULT_OPTIONS.copy(showInheritedFunctions = true, showInheritedProperties = true)),
            Triple("SuspendFunctions", suspendCode, DEFAULT_OPTIONS),
            Triple("SealedClasses", sealedClassesCode, DEFAULT_OPTIONS),
        ).forEach { (name, code, options) ->
            val note = buildString {
                appendLine("note as note_of_code")
                val nonDefaultOptions = options.asMap().filter { DEFAULT_OPTIONS.asMap()[it.key] != it.value }.entries
                if (nonDefaultOptions.isNotEmpty()){
                    appendLine("Non default options:")
                    appendLine(nonDefaultOptions.joinToString("\n") { "${it.key} = ${it.value}" })
                    appendLine()
                }
                appendLine("Kotlin Code:")
                appendLine(code)
                appendLine("end note")
            }
            newCompilation(options.copy(title = "Example for $name", prefix = GENERATION_REFERENCE_COMMENT, postfix = note), listOf(SourceFile.kotlin("$name.kt", code))).let {
                val result = it.compile()
                saveUMLInDocs(result.sourcesGeneratedBySymbolProcessor.first(), name)
            }
        }
    }

}