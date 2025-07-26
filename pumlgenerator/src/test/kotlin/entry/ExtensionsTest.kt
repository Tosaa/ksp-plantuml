package entry

import CompilationTest
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import uml.DiagramElement
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExtensionsTest : CompilationTest() {
    val animalInterfaceCode = """
    package explorer.database
    public interface Animal {
        val name : String
        val isExtinct : Boolean
        fun makeSound() : String
    }
    """
    val animalExtensionCode = """
    package explorer.database.extension
    import explorer.database.Animal
    public val Animal.isAlive : Boolean
        get() = !this.isExtinct

    public fun Animal.describe() : String = "${'\$'}name is an animal that makes ${'\$'}{makeSound()}}"
    """

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Extensions are shown as well`() {
        val files = listOf(
            SourceFile.kotlin("Animal.kt", animalInterfaceCode),
            SourceFile.kotlin("AnimalExtension.kt", animalExtensionCode)
        )
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile,"isAlive : Boolean")
        assertContains(generatedFile,"describe() : String")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Extensions for classes in external packages works`() {
        val files = listOf(
            SourceFile.kotlin("StringExtension.kt","""
package explorer.database            
public fun String.withPrefix(prefix:String): String = "prefix$this"
""".trimIndent())
        )
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile,DiagramElement.shellString)
        assertContains(generatedFile,"class \"String\" as kotlin_String")
        assertContains(generatedFile,"<ext> withPrefix(String) : String")
    }
}