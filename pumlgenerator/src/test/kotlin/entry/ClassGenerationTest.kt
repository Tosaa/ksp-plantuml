package entry

import CompilationTest
import assertContainsNot
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import generateClass
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClassGenerationTest : CompilationTest() {

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `create UML class entry without package`() {
        val expectedClassName = "TestClass"
        val packageName = ""
        val expectedAlias = "TestClass"
        val classDefinition = "class \"$expectedClassName\" as $expectedAlias"
        val expectedProperties = listOf("bar : Int", "bar2 : String")
        val expectedFunctions = listOf("foo() : Unit", "foo2() : String")
        val kotlinSource = SourceFile.kotlin(
            "TestClass.kt", generateClass(packageName, expectedClassName, listOf("val bar : Int = 4", "val bar2 : String \n get() = bar.toString()"), listOf("fun foo(): Unit", "fun foo2(): String"))
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
    fun `create UML class entry with package`() {
        val expectedClassName = "TestClass"
        val packageName = "test.a.b.c"
        val expectedAlias = "test_a_b_c_TestClass"
        val classDefinition = "class \"$expectedClassName\" as $expectedAlias"
        val expectedProperties = listOf("bar : Int", "bar2 : String")
        val expectedFunctions = listOf("foo() : Unit", "foo2(String) : String", "foo3(String, Int) : String")
        val kotlinSource = SourceFile.kotlin(
            "TestClass.kt", generateClass("test.a.b.c", expectedClassName, listOf("val bar : Int = 4", "val bar2 : String \n get() = bar.toString()"), listOf("fun foo(): Unit", "fun foo2(x:String): String", "fun foo3(x:String, y:Int): String"))
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
    fun `create UML class entry with inner class`() {
        val expectedOuterClassName = "TestClass"
        val expectedOuterClassAlias = "test_a_b_c_TestClass"
        val outerClassDefinition = "class \"$expectedOuterClassName\" as $expectedOuterClassAlias"
        val expectedInnerClassName = "TestClass.InnerTestClass"
        val expectedInnerClassAlias = "test_a_b_c_TestClass_InnerTestClass"
        val innerClassDefinition = "class \"$expectedInnerClassName\" as $expectedInnerClassAlias"
        val kotlinSource = SourceFile.kotlin(
            "TestClass.kt",
            generateClass(
                packageName = "test.a.b.c",
                className = "TestClass",
                properties = listOf("val x:Int = 3"),
                functions = listOf("fun foo() : Unit"),
                innerClassScope = generateClass(
                    packageName = "",
                    className = "InnerTestClass",
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
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, outerClassDefinition)
        assertContains(generatedFile, innerClassDefinition)
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `create UML class entry without excluded properties`() {
        val expectedClassName = "TestClass"
        val packageName = "test.a.b.c"
        val expectedAlias = "test_a_b_c_TestClass"
        val classDefinition = "class \"$expectedClassName\" as $expectedAlias"
        val expectedProperties = listOf("bar : Int")
        val expectedFunctions = listOf("foo() : Unit", "foo2(String) : String", "foo3(String, Int) : String")
        val kotlinSource = SourceFile.kotlin(
            "TestClass.kt", generateClass("test.a.b.c", expectedClassName, listOf("val bar : Int = 4", "val bar2 : String \n get() = bar.toString()"), listOf("fun foo(): Unit", "fun foo2(x:String): String", "fun foo3(x:String, y:Int): String"))
        )
        val compilation = newCompilation(DEFAULT_OPTIONS.copy(excludedPropertyNames = listOf("bar2")), listOf(kotlinSource))
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
        assertContainsNot(generatedFile, "bar2")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `create UML class entry without excluded function`() {
        val expectedClassName = "TestClass"
        val packageName = "test.a.b.c"
        val expectedAlias = "test_a_b_c_TestClass"
        val classDefinition = "class \"$expectedClassName\" as $expectedAlias"
        val expectedProperties = listOf("bar : Int", "bar2 : String")
        val expectedFunctions = listOf("foo() : Unit", "foo3(String, Int) : String")
        val kotlinSource = SourceFile.kotlin(
            "TestClass.kt", generateClass("test.a.b.c", expectedClassName, listOf("val bar : Int = 4", "val bar2 : String \n get() = bar.toString()"), listOf("fun foo(): Unit", "fun foo2(x:String): String", "fun foo3(x:String, y:Int): String"))
        )
        val compilation = newCompilation(DEFAULT_OPTIONS.copy(excludedFunctionNames = listOf("foo2")), listOf(kotlinSource))
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
        assertContainsNot(generatedFile, "foo2")
    }
}