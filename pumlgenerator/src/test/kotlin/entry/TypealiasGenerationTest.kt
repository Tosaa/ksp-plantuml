package entry

import CompilationTest
import assertContainsNot
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TypealiasGenerationTest : CompilationTest() {

    val typeAliasOfItem = """
        package basic
        typealias BigItem = Item
    """.trimIndent()

    val boxCode = """
        package basic
        interface Box {
            val index : Int
            val content : BigItem
        }
        interface Item {
            val description : String
        }
    """.trimIndent()
    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Typealias of classes can be resolved`() {
        val files = listOf(SourceFile.kotlin("BoxAlias.kt", typeAliasOfItem),SourceFile.kotlin("Box.kt", boxCode))
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "basic_Box --* basic_BigItem")
        assertContainsNot(generatedFile, "basic_Box --* box_Item")
    }

}