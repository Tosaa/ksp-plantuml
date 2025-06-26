package entry

import CompilationTest
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import generateEnum
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class TypeResolveTest : CompilationTest() {

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Optional types are identified correctly`() {
        val packageName = "maybe"
        val kotlinSource = SourceFile.kotlin(
            "Maybe.kt", """
                package $packageName
                class Maybe(val uniqueIdentifier : String) {
                    val optionalShortcut : String?
                        get() = uniqueIdentifier.takeIf{ it.length > 4 }?.let{ it.take(4) }
                    val optionalValue : Definitely?
                        get() = null
                }

                class Definitely(val value:Int)
            """.trimIndent()
        )
        val compilation = newCompilation(DEFAULT_OPTIONS, listOf(kotlinSource))
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile,"uniqueIdentifier : String")
        assertContains(generatedFile,"optionalShortcut : String?")
        assertContains(generatedFile,"optionalValue : Definitely?")
    }

}