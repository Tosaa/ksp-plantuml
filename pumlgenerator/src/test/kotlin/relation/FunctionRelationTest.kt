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
    val thingBox = """
    package com.other
    import com.one.OneThing
    public class ThingBox(){
        fun get() : Result<OneThing> {
            return Result.failure(IllegalStateException("Failed to retrieve OneThing"))
        }
    }
    """
    val boxWithThingsCode = """
        package com.other
        import kotlinx.coroutines.flow.* 
        data class BoxWithThings(val nameOfThings: List<String>) {
            fun otherThings(): List<OtherThing> = emptyList()
            fun otherThingsAsFlow(): Flow<OtherThing> = flow { }
        }
    """.trimIndent()


    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Create relations in functions between classes`() {
        val fileNames = listOf("Something.kt", "OneThing.kt", "OtherThing.kt", "ThingBox.kt")
        val codes = listOf(somethingCode, onethingCode, otherthingCode, thingBox)
        val files = fileNames.zip(codes).map { (name, code) ->
            SourceFile.kotlin(name, code)
        }.toList()
        val compilation = newCompilation(Options(), files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")

        assertContains(generatedFile, "com_Something --> com_one_OneThing")
        assertContains(generatedFile, "com_one_OneThing --> com_other_OtherThing")
        assertContains(generatedFile, "com_other_ThingBox ..> com_one_OneThing")
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
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
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
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
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
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
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
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContainsNot(generatedFile, "com_other_OtherThing --> com_other_OtherThing")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Do not create indirect relations if disabled`() {
        val fileNames = listOf("Something.kt", "OneThing.kt", "OtherThing.kt", "ThingBox.kt")
        val codes = listOf(somethingCode, onethingCode, otherthingCode, thingBox)
        val files = fileNames.zip(codes).map { (name, code) ->
            SourceFile.kotlin(name, code)
        }.toList()
        val compilation = newCompilation(Options(showIndirectRelations = false), files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "com_Something --> com_one_OneThing")
        assertContains(generatedFile, "com_one_OneThing --> com_other_OtherThing")
        assertContainsNot(generatedFile, "com_other_ThingBox ..> com_one_OneThing")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Create Indirect relations if List is returned`() {
        val fileNames = listOf("OtherThing.kt", "BoxWithThings.kt")
        val codes = listOf(otherthingCode, boxWithThingsCode)
        val files = fileNames.zip(codes).map { (name, code) ->
            SourceFile.kotlin(name, code)
        }.toList()
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "otherThings() : List<OtherThing>")
        assertContains(generatedFile, "com_other_BoxWithThings ..> com_other_OtherThing")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Create Indirect relations if List is returned with reduced included packages`() {
        val fileNames = listOf("OtherThing.kt", "BoxWithThings.kt")
        val codes = listOf(otherthingCode, boxWithThingsCode)
        val files = fileNames.zip(codes).map { (name, code) ->
            SourceFile.kotlin(name, code)
        }.toList()
        val compilation = newCompilation(DEFAULT_OPTIONS.copy(includedPackages = listOf("com.other")), files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "otherThings() : List<OtherThing>")
        assertContains(generatedFile, "com_other_BoxWithThings ..> com_other_OtherThing")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Create Indirect relations if Flow is returned`() {
        val fileNames = listOf("OtherThing.kt", "BoxWithThings.kt")
        val codes = listOf(otherthingCode, boxWithThingsCode)
        val files = fileNames.zip(codes).map { (name, code) ->
            SourceFile.kotlin(name, code)
        }.toList()
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "otherThingsAsFlow() : Flow<OtherThing>")
        assertContains(generatedFile, "com_other_BoxWithThings ..> com_other_OtherThing")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Create Indirect relations if Flow is returned with reduced included packages`() {
        val fileNames = listOf("OtherThing.kt", "BoxWithThings.kt")
        val codes = listOf(otherthingCode, boxWithThingsCode)
        val files = fileNames.zip(codes).map { (name, code) ->
            SourceFile.kotlin(name, code)
        }.toList()
        val compilation = newCompilation(DEFAULT_OPTIONS.copy(includedPackages = listOf("com.other")), files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "otherThingsAsFlow() : Flow<OtherThing>")
        assertContains(generatedFile, "com_other_BoxWithThings ..> com_other_OtherThing")
    }
}