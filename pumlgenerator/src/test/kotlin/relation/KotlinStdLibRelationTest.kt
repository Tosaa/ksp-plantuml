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

class KotlinStdLibRelationTest : CompilationTest() {

    val code = """
    package box
    public interface Item
    public interface Box {
        val foo : Unit
        val openingFunction : (Box) -> Unit
        fun open(): Unit
        fun putInto():Result<Item>
    }
    """


    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Unit & Result is not a referenced class nor relations are created`() {
        val files = listOf(SourceFile.kotlin("Code.kt", code))
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "foo : Unit")
        assertContains(generatedFile, "openingFunction : ")
        assertContains(generatedFile, "open() : Unit")
        assertContains(generatedFile, "putInto() : Result<Item>")
        assertContainsNot(generatedFile, "box_Box --* kotlin_Unit")
        assertContainsNot(generatedFile, "box_Box --* kotlin_Result")
        assertContains(generatedFile, "box_Box ..> box_Item")
    }

}