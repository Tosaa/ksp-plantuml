package entry

import CompilationTest
import assertContainsNot
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import generateObject
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObjectGenerationTest : CompilationTest() {

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `create UML object entry without package`() {
        val expectedClassName = "TestObject"
        val packageName = ""
        val expectedAlias = "TestObject"
        val classDefinition = "class \"$expectedClassName\" as $expectedAlias"
        val expectedProperties = listOf("bar : Int", "bar2 : String")
        val expectedFunctions = listOf("foo() : Unit", "foo2() : String")
        val kotlinSource = SourceFile.kotlin(
            "TestObject.kt", generateObject(packageName, expectedClassName, listOf("val bar : Int = 4", "val bar2 : String \n get() = bar.toString()"), listOf("fun foo(): Unit", "fun foo2(): String"))
        )
        val compilation = newCompilation(DEFAULT_OPTIONS, listOf(kotlinSource))
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertTrue { generatedFile.contains(Regex("$classDefinition.*<<.*object>>")) }
        expectedFunctions.forEach {
            assertContains(generatedFile, it)
        }
        expectedProperties.forEach {
            assertContains(generatedFile, it)
        }
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `create UML object entry with package`() {
        val expectedClassName = "TestObject"
        val packageName = "test.a.b.c"
        val expectedAlias = "test_a_b_c_TestObject"
        val classDefinition = "class \"$expectedClassName\" as $expectedAlias"
        val expectedProperties = listOf("bar : Int", "bar2 : String")
        val expectedFunctions = listOf("foo() : Unit", "foo2(String) : String", "foo3(String, Int) : String")
        val kotlinSource = SourceFile.kotlin(
            "TestObject.kt", generateObject("test.a.b.c", expectedClassName, listOf("val bar : Int = 4", "val bar2 : String \n get() = bar.toString()"), listOf("fun foo(): Unit", "fun foo2(x:String): String", "fun foo3(x:String, y:Int): String"))
        )
        val compilation = newCompilation(DEFAULT_OPTIONS, listOf(kotlinSource))
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
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
    fun `create UML object entry with inner class`() {
        val expectedOuterClassName = "TestObject"
        val expectedOuterClassAlias = "test_a_b_c_TestObject"
        val outerClassDefinition = "class \"$expectedOuterClassName\" as $expectedOuterClassAlias"
        val expectedInnerClassName = "TestObject.InnerTestObject"
        val expectedInnerClassAlias = "test_a_b_c_TestObject_InnerTestObject"
        val innerClassDefinition = "class \"$expectedInnerClassName\" as $expectedInnerClassAlias"
        val kotlinSource = SourceFile.kotlin(
            "TestObject.kt",
            generateObject(
                packageName = "test.a.b.c",
                className = "TestObject",
                properties = listOf("val x:Int = 3"),
                functions = listOf("fun foo() : Unit"),
                innerClassScope = generateObject(
                    packageName = "",
                    className = "InnerTestObject",
                    properties = listOf("val bar : Int = 4", "private val bar2 : String \n get() = bar.toString()"),
                    functions = listOf("fun foo(): Unit", "internal fun foo2(x:String): String", "fun foo3(x:String, y:Int): String"),
                )
            )
        ).also { println(it) }
        val compilation = newCompilation(DEFAULT_OPTIONS, listOf(kotlinSource))

        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, outerClassDefinition)
        assertContains(generatedFile, innerClassDefinition)
    }

}