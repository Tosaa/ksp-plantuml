package entry

import CompilationTest
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import generateInterface
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InterfaceGenerationTest : CompilationTest() {

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `create UML interface entry without package`() {
        val expectedClassName = "TestInterface"
        val packageName = ""
        val expectedAlias = "TestInterface"
        val classDefinition = "interface \"$expectedClassName\" as $expectedAlias"
        val expectedProperties = listOf("bar : Int", "bar2 : String")
        val expectedFunctions = listOf("foo() : Unit", "foo2() : String")
        val kotlinSource = SourceFile.kotlin(
            "TestInterface.kt", generateInterface(packageName, expectedClassName, listOf("val bar : Int", "val bar2 : String"), listOf("fun foo(): Unit", "fun foo2(): String"))
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

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `create UML interface entry with package`() {
        val expectedClassName = "TestInterface"
        val packageName = "test.a.b.c"
        val expectedAlias = "test_a_b_c_TestInterface"
        val classDefinition = "interface \"$expectedClassName\" as $expectedAlias"
        val expectedProperties = listOf("bar : Int", "bar2 : String")
        val expectedFunctions = listOf("foo() : Unit", "foo2(String) : String", "foo3(String, Int) : String")
        val kotlinSource = SourceFile.kotlin(
            "TestInterface.kt", generateInterface("test.a.b.c", expectedClassName, listOf("val bar : Int ", "val bar2 : String"), listOf("fun foo(): Unit", "fun foo2(x:String): String", "fun foo3(x:String, y:Int): String"))
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

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `create UML interface entry with inner class`() {
        val expectedOuterClassName = "TestInterface"
        val expectedOuterClassAlias = "test_a_b_c_TestInterface"
        val outerClassDefinition = "interface \"$expectedOuterClassName\" as $expectedOuterClassAlias"
        val expectedInnerClassName = "TestInterface.InnerTestInterface"
        val expectedInnerClassAlias = "test_a_b_c_TestInterface_InnerTestInterface"
        val innerClassDefinition = "interface \"$expectedInnerClassName\" as $expectedInnerClassAlias"
        val kotlinSource = SourceFile.kotlin(
            "TestInterface.kt",
            generateInterface(
                packageName = "test.a.b.c",
                className = "TestInterface",
                properties = listOf("val x:Int"),
                functions = listOf("fun foo() : Unit"),
                innerClassScope = generateInterface(
                    packageName = "",
                    className = "InnerTestInterface",
                    properties = listOf("val bar : Int ", "val bar2 : String"),
                    functions = listOf("fun foo(): Unit", "fun foo2(x:String): String", "fun foo3(x:String, y:Int): String"),
                )
            )
        ).also { println(it) }
        val compilation = newCompilation(DEFAULT_OPTIONS, listOf(kotlinSource))

        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, outerClassDefinition)
        assertContains(generatedFile, innerClassDefinition)
    }

}