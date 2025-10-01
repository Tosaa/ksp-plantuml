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

class SealedGenerationTest : CompilationTest() {

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `create simple sealed class with package`() {
        val packageName = "test.letters"
        val kotlinSource = SourceFile.kotlin(
            "ABC.kt", """
                package $packageName
                sealed class ABC {
                    data object A : ABC()
                    class B(val test:String) : ABC()
                    class C(val bar:Int) : ABC()
                }
            """.trimIndent()
        )
        val compilation = newCompilation(DEFAULT_OPTIONS, listOf(kotlinSource))
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertTrue { generatedFile.contains(Regex("class \"ABC\" as test_letters_ABC.*Sealed>>")) }
        assertTrue { generatedFile.contains(Regex("class \"ABC.A\" as test_letters_ABC_A.*object>>")) }
        assertTrue { generatedFile.contains(Regex("class \"ABC.B\" as test_letters_ABC_B")) }
        assertTrue { generatedFile.contains(Regex("class \"ABC.C\" as test_letters_ABC_C")) }
    }

}