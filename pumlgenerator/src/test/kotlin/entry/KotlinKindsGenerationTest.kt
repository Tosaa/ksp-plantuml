package entry

import CompilationTest
import Notation
import Options
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

/**
 * Verifies the rendering of Kotlin specific declaration kinds:
 * abstract classes, sealed classes, value classes, fun interfaces,
 * annotation classes and data objects.
 */
class KotlinKindsGenerationTest : CompilationTest() {

    private val kindsCode = """
    package kinds

    abstract class Animal {
        abstract val name: String
        abstract fun sound(): String
        fun breathe(): Unit {}
    }

    sealed class Response

    @JvmInline
    value class Meters(val value: Double)

    fun interface Clickable {
        fun click(): Unit
    }

    annotation class Marker

    data object Nothingness
    """

    @OptIn(ExperimentalCompilerApi::class)
    private fun generate(options: Options): String {
        val compilation = newCompilation(options, listOf(SourceFile.kotlin("Kinds.kt", kindsCode)))
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        return result.sourcesGeneratedBySymbolProcessor.first().readText()
    }

    @Test
    fun `Kotlin declaration kinds are rendered with their stereotypes`() {
        val generatedFile = generate(DEFAULT_OPTIONS)

        // abstract classes are rendered as abstract with {abstract} members
        assertContains(generatedFile, "abstract class \"Animal\" as kinds_Animal")
        assertContains(generatedFile, "{abstract} name : String")
        assertContains(generatedFile, "{abstract} sound() : String")

        // sealed classes cannot be instantiated -> abstract by definition
        assertContains(generatedFile, "abstract class \"Response\" as kinds_Response <<Sealed>>")

        // value classes
        assertContains(generatedFile, "class \"Meters\" as kinds_Meters <<Value>>")

        // fun interfaces
        assertContains(generatedFile, "interface \"Clickable\" as kinds_Clickable <<Fun>>")

        // annotation classes
        assertContains(generatedFile, "annotation \"Marker\" as kinds_Marker {")

        // data objects
        assertContains(generatedFile, "class \"Nothingness\" as kinds_Nothingness << (O, #FF7700) data object>>")
    }

    @Test
    fun `The keyword of UML is written in front of the stereotypes in the notation of UML`() {
        val generatedFile = generate(DEFAULT_OPTIONS.copy(notation = Notation.UML))
        assertContains(generatedFile, "interface \"Clickable\" as kinds_Clickable <<interface, Fun>>")
        assertContains(generatedFile, "annotation \"Marker\" as kinds_Marker <<annotation>>")
        assertContains(generatedFile, "abstract class \"Response\" as kinds_Response <<Sealed>>")
    }
}
