package docgeneration

import Options
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import options.OptionsTest
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class FileCanBeSavedTest : OptionsTest() {

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `file name can be provided through options`() {
        val compilation = newCompilation(Options(title = "Diagram without anything", prefix = "'start", postfix = "'end", outputFileName = "here.puml"), sources = emptyList())
        compilation.compile()
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        assertTrue(result.sourcesGeneratedBySymbolProcessor.toList().any { it.name == "here.puml" })
        val puml = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(puml, "title Diagram without anything")
        assertContains(puml, "'start")
        assertContains(puml, "'end")
    }
}