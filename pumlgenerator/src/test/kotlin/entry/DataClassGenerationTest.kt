package entry

import CompilationTest
import com.tschuchort.compiletesting.BuildConfig
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import generateClass
import generateDataClass
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.incremental.createDirectory
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DataClassGenerationTest : CompilationTest() {

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `create UML class entry for data class`() {
        val expectedClassName = "TestClass"
        val packageName = "data"
        val expectedAlias = "data_TestClass"
        val classDefinition = "class \"$expectedClassName\" as $expectedAlias <<Data>>"
        val expectedProperties = listOf("bar : Int", "bar2 : String")
        val expectedFunctions = listOf("foo() : Unit", "foo2() : String")
        val kotlinSource = SourceFile.kotlin(
            "TestClass.kt", generateDataClass(packageName, expectedClassName, listOf("val bar : Int = 4", "val bar2 : String"), listOf("fun foo(): Unit", "fun foo2(): String"))
        )
        val compilation = newCompilation(DEFAULT_OPTIONS, listOf(kotlinSource))
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, classDefinition)
        expectedFunctions.forEach {
            assertContains(generatedFile, it)
        }
        expectedProperties.forEach {
            assertContains(generatedFile, it)
        }
    }
}