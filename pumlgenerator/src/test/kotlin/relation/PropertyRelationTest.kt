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

class PropertyRelationTest : CompilationTest() {
    val somethingCode = """
    package com
    import com.other.OtherThing
    import com.one.OneThing
    public interface Something {
        val a : OneThing
        val b : OtherThing
        val c : List<OtherThing>
        val d : Something?
        val e : Something
        fun findAThing() : OneThing
    }
    """
    val onethingCode = """
    package com.one
    import com.other.OtherThing
    interface OneThing {
        val otherThing : OtherThing
        val description : String
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
        val things : List<OneThing> = emptyList()
    }
    """

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Create relations between classes`() {
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
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "com_Something --* com_one_OneThing")
        assertContains(generatedFile, "com_Something --* com_other_OtherThing")
        assertContains(generatedFile, "com_one_OneThing --* com_other_OtherThing")
        assertContains(generatedFile, "com_other_ThingBox ..* com_one_OneThing")
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
        assertContains(generatedFile, "com_Something --* com_one_OneThing")
        assertContainsNot(generatedFile, "com_Something --* com_other_OtherThing")
        assertContainsNot(generatedFile, "com_one_OneThing --* com_other_OtherThing")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Do not create a relations between classes if its disabled`() {
        val fileNames = listOf("Something.kt", "OneThing.kt", "OtherThing.kt")
        val codes = listOf(somethingCode, onethingCode, otherthingCode)
        val files = fileNames.zip(codes).map { (name, code) ->
            SourceFile.kotlin(name, code)
        }.toList()
        val compilation = newCompilation(Options(showPropertyRelations = false), files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContainsNot(generatedFile, "com_Something --* com_one_OneThing")
        assertContainsNot(generatedFile, "com_Something --* com_other_OtherThing")
        assertContainsNot(generatedFile, "com_one_OneThing --* com_other_OtherThing")
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
        assertContainsNot(generatedFile, "com_Something --* com_Something")
        assertContainsNot(generatedFile, "com_Something --* com_Something")
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
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "com_Something --* com_one_OneThing")
        assertContains(generatedFile, "com_Something --* com_other_OtherThing")
        assertContains(generatedFile, "com_one_OneThing --* com_other_OtherThing")
        assertContainsNot(generatedFile, "com_other_ThingBox ..* com_one_OneThing")
    }
}