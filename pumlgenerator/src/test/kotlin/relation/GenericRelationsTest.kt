package relation

import CompilationTest
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenericRelationsTest : CompilationTest() {
    val code = """
    package explorer.database
    data class Item(val index : Int)
    data class Text(val text:String)
    public interface Box {
        val description: Text
        val label : Pair<Text, Text>
        val items : List<Item>
        val legend : List<Pair<Int, Item>>
    }
    """

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Generics are resolved and shown correcly`() {
        val files = listOf(SourceFile.kotlin("Code.kt", code))
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "description : Text")
        assertContains(generatedFile, "label : Pair<Text,Text>")
        assertContains(generatedFile, "items : List<Item>")
        assertContains(generatedFile, "legend : List<Pair<Int,Item>>")
        assertContains(generatedFile,"explorer_database_Box::description --* explorer_database_Text")
        assertContains(generatedFile,"explorer_database_Box::items --* explorer_database_Item")

        assertContains(generatedFile,"<> d_explorer_database_Box__legend_kotlin_Pair")
        assertContains(generatedFile,"explorer_database_Box::legend - d_explorer_database_Box__legend_kotlin_Pair")
        assertContains(generatedFile,"d_explorer_database_Box__legend_kotlin_Pair --* kotlin_Int")
        assertContains(generatedFile,"d_explorer_database_Box__legend_kotlin_Pair --* explorer_database_Item")

        assertContains(generatedFile,"<> d_explorer_database_Box__label_kotlin_Pair")
        assertContains(generatedFile,"explorer_database_Box::label - d_explorer_database_Box__label_kotlin_Pair")
        assertContains(generatedFile,"d_explorer_database_Box__label_kotlin_Pair --* explorer_database_Text")

    }

}