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

class EnumGenerationTest : CompilationTest() {

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `create simple UML enum with package`() {
        val expectedClassName = "ABC"
        val packageName = "test.letters"
        val expectedAlias = "test_letters_ABC"
        val classDefinition = "enum \"$expectedClassName\" as $expectedAlias"
        val entries = listOf("A", "B", "C")
        val kotlinSource = SourceFile.kotlin(
            "ABC.kt", generateEnum(packageName, expectedClassName, entries, listOf(), listOf())
        )
        val compilation = newCompilation(DEFAULT_OPTIONS, listOf(kotlinSource))
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, classDefinition)

        entries.forEach {
            generatedFile.lines().any {
                it.trim().equals(it)
            }
        }
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `create UML enum without package`() {
        val expectedClassName = "ABC"
        val packageName = ""
        val expectedAlias = "ABC"
        val classDefinition = "enum \"$expectedClassName\" as $expectedAlias"
        val expectedProperties = listOf("bar : Int", "bar2 : String")
        val expectedFunctions = listOf("foo() : Unit", "foo2() : String")
        val entries = listOf("A", "B", "C")
        val kotlinSource = SourceFile.kotlin(
            "ABC.kt", generateEnum(packageName, expectedClassName, entries, listOf("val bar : Int = 4", "val bar2 : String \n get() = bar.toString()"), listOf("fun foo(): Unit", "fun foo2(): String"))
        )
        val compilation = newCompilation(DEFAULT_OPTIONS, listOf(kotlinSource))
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, classDefinition)
        entries.forEach {
            assertContains(generatedFile, it)
        }
        expectedFunctions.forEach {
            assertContains(generatedFile, it)
        }
        expectedProperties.forEach {
            assertContains(generatedFile, it)
        }
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `create UML enum with package`() {
        val expectedClassName = "ABC"
        val packageName = "test.a.b.c"
        val expectedAlias = "test_a_b_c_ABC"
        val classDefinition = "enum \"$expectedClassName\" as $expectedAlias"
        val expectedProperties = listOf("bar : Int", "bar2 : String")
        val expectedFunctions = listOf("foo() : Unit", "foo2(String) : String", "foo3(String, Int) : String")
        val entries = listOf("A", "B", "C")
        val kotlinSource = SourceFile.kotlin(
            "ABC.kt", generateEnum(packageName, expectedClassName, entries, listOf("val bar : Int = 4", "val bar2 : String \n get() = bar.toString()"), listOf("fun foo(): Unit", "fun foo2(x:String): String", "fun foo3(x:String, y:Int): String"))
        )
        val compilation = newCompilation(DEFAULT_OPTIONS, listOf(kotlinSource))
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, classDefinition)
        entries.forEach {
            assertContains(generatedFile, it)
        }
        expectedFunctions.forEach {
            assertContains(generatedFile, it)
        }
        expectedProperties.forEach {
            assertContains(generatedFile, it)
        }
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `enum default properties and functions are shown`() {
        val expectedClassName = "ABC"
        val packageName = "test.a.b.c"
        val expectedAlias = "test_a_b_c_ABC"
        val classDefinition = "enum \"$expectedClassName\" as $expectedAlias"
        val entries = listOf("A", "B", "C")
        val kotlinSource = SourceFile.kotlin(
            "ABC.kt", generateEnum(packageName, expectedClassName, entries, emptyList(), emptyList())
        )
        val compilation = newCompilation(DEFAULT_OPTIONS, listOf(kotlinSource))
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, classDefinition)
        entries.forEach {
            assertContains(generatedFile, it)
        }
        assertContains(generatedFile, "name : String")
        assertContains(generatedFile, "ordinal : Int")
        assertContains(generatedFile, "{static} entries : EnumEntries<ABC>")
    }

}