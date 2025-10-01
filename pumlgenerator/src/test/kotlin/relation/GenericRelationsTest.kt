package relation

import CompilationTest
import assertContainsNot
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenericRelationsTest : CompilationTest() {
    private val codeWithCollections = """
    package explorer.database
    data class Item(val index : Int)
    data class Text(val text: String)
    public interface Box {
        val description: Text
        val label : Pair<Text, Text>
        val items : List<Item>
        val legend : List<Pair<Int, Item>>
    }
    """

    private val codeWithGenerics = """
    package explorer.database
    sealed class Color(val rgbHex:String){
        object RED : Color("ff0000")
        object GREEN : Color("00ff00")
        object BLUE : Color("0000ff")
    }
    data class Item<T:Color>(val index : Int)
    public interface Box {
        val item : Item<Color>
        val redItem : Item<Color.RED>
        val blueItems: List<Item<Color.BLUE>>
    }
    """

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Collections are resolved and shown correcly`() {
        val files = listOf(SourceFile.kotlin("Code.kt", codeWithCollections))
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "description : Text")
        assertContains(generatedFile, "label : Pair<Text,Text>")
        assertContains(generatedFile, "items : List<Item>")
        assertContains(generatedFile, "legend : List<Pair<Int,Item>>")
        assertContains(generatedFile, "explorer_database_Box --* explorer_database_Text")
        assertContains(generatedFile, "explorer_database_Box ..* explorer_database_Item")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Generics are resolved and shown correcly`() {
        val files = listOf(SourceFile.kotlin("Code.kt", codeWithGenerics))
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")

        assertContains(generatedFile, "item : Item<Color>")
        assertContains(generatedFile, "redItem : Item<RED>")
        assertContains(generatedFile, "blueItems : List<Item<BLUE>>")
        assertContains(generatedFile, "explorer_database_Box --* explorer_database_Item")
        assertContains(generatedFile, "explorer_database_Box ..* explorer_database_Color")
        assertContains(generatedFile, "explorer_database_Box ..* explorer_database_Color_RED")
        assertContains(generatedFile, "explorer_database_Box ..* explorer_database_Color_BLUE")
    }

}
