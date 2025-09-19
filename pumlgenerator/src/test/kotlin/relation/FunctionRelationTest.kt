package relation

import CompilationTest
import Options
import assertContainsNot
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FunctionRelationTest : CompilationTest() {
    val somethingCode = """
    package com
    import com.other.OtherThing
    import com.one.OneThing
    public interface Something {
        fun findAThing() : OneThing
    }
    """
    val onethingCode = """
    package com.one
    import com.other.OtherThing
    interface OneThing {
        fun findOtherThing() : OtherThing
        fun retrieveDescription() : String
        fun copy() : OneThing
    }
    """
    val otherthingCode = """
    package com.other
    public data class OtherThing(val name:String, val isKnown:Boolean)
    """


    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Create relations in functions between classes`() {
        val fileNames = listOf("Something.kt", "OneThing.kt", "OtherThing.kt")
        val codes = listOf(somethingCode, onethingCode, otherthingCode)
        val files = fileNames.zip(codes).map { (name, code) ->
            SourceFile.kotlin(name, code)
        }.toList()
        val compilation = newCompilation(Options(), files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")

        assertContains(generatedFile, "com_Something --> com_one_OneThing")
        assertContains(generatedFile, "com_one_OneThing --> com_other_OtherThing")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Do not create a relations between classes if type is not part of the Rendered classes`() {
        val fileNames = listOf("Something.kt", "OneThing.kt", "OtherThing.kt")
        val codes = listOf(somethingCode, onethingCode, otherthingCode)
        val files = fileNames.zip(codes).map { (name, code) ->
            SourceFile.kotlin(name, code)
        }.toList()
        val compilation = newCompilation(Options(excludedPackages = listOf("com.other")), files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "com_Something --> com_one_OneThing")
        assertContainsNot(generatedFile, "com_one_OneThing --> com_other_OtherThing")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Do not create a relations between classes if its disabled`() {
        val fileNames = listOf("Something.kt", "OneThing.kt", "OtherThing.kt")
        val codes = listOf(somethingCode, onethingCode, otherthingCode)
        val files = fileNames.zip(codes).map { (name, code) ->
            SourceFile.kotlin(name, code)
        }.toList()
        val compilation = newCompilation(Options(showFunctionRelations = false), files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContainsNot(generatedFile, "com_Something --> com_one_OneThing")
        assertContainsNot(generatedFile, "com_one_OneThing --> com_other_OtherThing")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `create relations between classes but ignore inheritance`() {
        val fileNames = listOf("Something.kt", "OneThing.kt", "OtherThing.kt")
        val codes = listOf(somethingCode, onethingCode, otherthingCode)
        val files = fileNames.zip(codes).map { (name, code) ->
            SourceFile.kotlin(name, code)
        }.toList()
        val compilation = newCompilation(Options(showInheritance = false, showFunctionRelations = true), files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "com_Something --> com_one_OneThing")
        assertContains(generatedFile, "com_one_OneThing --> com_other_OtherThing")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Do not create a relations to itself`() {
        val fileNames = listOf("Something.kt", "OneThing.kt", "OtherThing.kt")
        val codes = listOf(somethingCode, onethingCode, otherthingCode)
        val files = fileNames.zip(codes).map { (name, code) ->
            SourceFile.kotlin(name, code)
        }.toList()
        val compilation = newCompilation(Options(), files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContainsNot(generatedFile, "com_other_OtherThing --> com_other_OtherThing")
    }
}