package relation

import CompilationTest
import Options
import assertContainsNot
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PropertyRelationTest : CompilationTest() {
    val somethingCode = """
    package com
    import com.other.OtherThing
    import com.one.OneThing
    public interface Something {
        val a : OneThing
        val b : OtherThing
        val c : List<OtherThing>
        val d : Something?
        val e : Something
        fun findAThing() : OneThing
    }
    """

    val onethingCode = """
    package com.one
    import com.other.OtherThing
    interface OneThing {
        val otherThing : OtherThing
        val description : String
    }
    """

    val otherthingCode = """
    package com.other
    public data class OtherThing(val name:String, val isKnown:Boolean)
    """

    val thingBox = """
    package com.other
    import com.one.OneThing
    public class ThingBox(){
        val things : List<OneThing> = emptyList()
    }
    """

    val classWithLambdaPrimitiveCode = """
    package com.other
    public class StringFactory(val name: String) {
        val functionNothingToString: () -> String = { name }
        val functionNothingToUnit: () -> Unit = {}
        val functionStringToUnit: (String) -> Unit = {_ -> Unit}
        val functionStringToString: (String) -> String = { _ -> name }
        val functionStringStringToString: (String, String) -> String = { _, _ -> name }
    }
    """

    val classWithLambdaCode = """
    package com.other
    import com.one.OneThing
    public class OneThingFactory(val oneThing: OneThing) {
        val functionNothingToOneThing: () -> OneThing = { oneThing }
        val functionNothingToUnit: () -> Unit = {}
        val functionOneThingToUnit: (OneThing) -> Unit = {_ -> Unit}
        val functionOneThingToOneThing: (OneThing) -> OneThing = { _ -> oneThing }
        val functionOneThingOneThingToOneThing: (OneThing, OneThing) -> OneThing = { _, _ -> oneThing }
    }
    """

    val interfaceWithFlow = """
    package com.test
    import kotlinx.coroutines.flow.*
    data class Color(val hex:String)
    data class Brightness(val value:Int)
    interface TestInterface {
        val colors : Flow<List<Color>>
        val brightness : StateFlow<Brightness>
    }
    """


    val classInPackageBCode = """
        package com.b
        class Test(){}
    """.trimIndent()

    val ClassInPackageACode = """
        package com.a
        import com.b.Test
        
        class TestRun(val test:Test){}
    """.trimIndent()


    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Create relations between classes`() {
        val fileNames = listOf("Something.kt", "OneThing.kt", "OtherThing.kt", "ThingBox.kt")
        val codes = listOf(somethingCode, onethingCode, otherthingCode, thingBox)
        val files = fileNames.zip(codes).map { (name, code) ->
            SourceFile.kotlin(name, code)
        }.toList()
        val compilation = newCompilation(Options(), files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "com_Something --* com_one_OneThing")
        assertContains(generatedFile, "com_Something --* com_other_OtherThing")
        assertContains(generatedFile, "com_one_OneThing --* com_other_OtherThing")
        assertContains(generatedFile, "com_other_ThingBox ..* com_one_OneThing")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Do not create a relations between classes if type is not part of the Rendered classes`() {
        val fileNames = listOf("Something.kt", "OneThing.kt", "OtherThing.kt")
        val codes = listOf(somethingCode, onethingCode, otherthingCode)
        val files = fileNames.zip(codes).map { (name, code) ->
            SourceFile.kotlin(name, code)
        }.toList()
        val compilation = newCompilation(Options(excludedPackages = listOf("com.other")), files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "com_Something --* com_one_OneThing")
        assertContainsNot(generatedFile, "com_Something --* com_other_OtherThing")
        assertContainsNot(generatedFile, "com_one_OneThing --* com_other_OtherThing")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Do not create a relations between classes if its disabled`() {
        val fileNames = listOf("Something.kt", "OneThing.kt", "OtherThing.kt")
        val codes = listOf(somethingCode, onethingCode, otherthingCode)
        val files = fileNames.zip(codes).map { (name, code) ->
            SourceFile.kotlin(name, code)
        }.toList()
        val compilation = newCompilation(Options(showPropertyRelations = false), files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContainsNot(generatedFile, "com_Something --* com_one_OneThing")
        assertContainsNot(generatedFile, "com_Something --* com_other_OtherThing")
        assertContainsNot(generatedFile, "com_one_OneThing --* com_other_OtherThing")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Do not create a relations to itself`() {
        val fileNames = listOf("Something.kt", "OneThing.kt", "OtherThing.kt")
        val codes = listOf(somethingCode, onethingCode, otherthingCode)
        val files = fileNames.zip(codes).map { (name, code) ->
            SourceFile.kotlin(name, code)
        }.toList()
        val compilation = newCompilation(Options(), files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContainsNot(generatedFile, "com_Something --* com_Something")
        assertContainsNot(generatedFile, "com_Something --* com_Something")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Do not create indirect relations if disabled`() {
        val fileNames = listOf("Something.kt", "OneThing.kt", "OtherThing.kt", "ThingBox.kt")
        val codes = listOf(somethingCode, onethingCode, otherthingCode, thingBox)
        val files = fileNames.zip(codes).map { (name, code) ->
            SourceFile.kotlin(name, code)
        }.toList()
        val compilation = newCompilation(Options(showIndirectRelations = false), files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "com_Something --* com_one_OneThing")
        assertContains(generatedFile, "com_Something --* com_other_OtherThing")
        assertContains(generatedFile, "com_one_OneThing --* com_other_OtherThing")
        assertContainsNot(generatedFile, "com_other_ThingBox ..* com_one_OneThing")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Test Generation of Interface with Flow Variable`() {
        val kotlinSource = SourceFile.kotlin(
            "SymbolProcessor.kt",
            interfaceWithFlow
        ).also { println(it) }
        val compilation = newCompilation(DEFAULT_OPTIONS, listOf(kotlinSource))

        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "colors : Flow<List<Color>>")
        assertContains(generatedFile, "com_test_TestInterface ..* com_test_Color")
        assertContains(generatedFile, "brightness : StateFlow<Brightness>")
        assertContains(generatedFile, "com_test_TestInterface ..* com_test_Brightness")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Test Generation of Interface with Flow Variable with reduced included packages`() {
        val kotlinSource = SourceFile.kotlin(
            "SymbolProcessor.kt",
            interfaceWithFlow
        ).also { println(it) }
        val compilation = newCompilation(DEFAULT_OPTIONS.copy(includedPackages = listOf("com.test")), listOf(kotlinSource))

        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "colors : Flow<List<Color>>")
        assertContains(generatedFile, "com_test_TestInterface ..* com_test_Color")
        assertContains(generatedFile, "brightness : StateFlow<Brightness>")
        assertContains(generatedFile, "com_test_TestInterface ..* com_test_Brightness")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Test Lambda functions with primitive types are shown in a Kotlin Manner`() {
        val kotlinSource = SourceFile.kotlin(
            "CodeWithLambda.kt",
            classWithLambdaPrimitiveCode
        ).also { println(it) }
        val compilation = newCompilation(DEFAULT_OPTIONS, listOf(kotlinSource))

        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "functionNothingToString : () -> String")
        assertContains(generatedFile, "functionNothingToUnit : () -> Unit")
        assertContains(generatedFile, "functionStringToUnit : (String) -> Unit")
        assertContains(generatedFile, "functionStringToString : (String) -> String")
        assertContains(generatedFile, "functionStringStringToString : (String, String) -> String")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Test Lambda functions with classes are shown in a Kotlin Manner`() {
        val fileNames = listOf("Something.kt", "OneThing.kt", "OtherThing.kt", "ClassWithLambda.kt")
        val codes = listOf(somethingCode, onethingCode, otherthingCode, classWithLambdaCode)
        val files = fileNames.zip(codes).map { (name, code) ->
            SourceFile.kotlin(name, code)
        }.toList()

        val compilation = newCompilation(DEFAULT_OPTIONS, files)

        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "functionNothingToOneThing : () -> OneThing")
        assertContains(generatedFile, "functionNothingToUnit : () -> Unit")
        assertContains(generatedFile, "functionOneThingToUnit : (OneThing) -> Unit")
        assertContains(generatedFile, "functionOneThingToOneThing : (OneThing) -> OneThing")
        assertContains(generatedFile, "functionOneThingOneThingToOneThing : (OneThing, OneThing) -> OneThing")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `A function that returns a function is shown in a Kotlin Manner`() {
        val kotlinSource = SourceFile.kotlin(
            "CodeThatReturnsFunction.kt",
            """
         interface TextStrategy {
             fun doMagic(magic: (String) -> String): () -> String
             fun createTextAppendingFunction(): () -> String
             fun createTextAppendingFunction(prefix: String): () -> String
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
        assertContains(generatedFile, "doMagic((String) -> String) : () -> String")
        assertContains(generatedFile, "createTextAppendingFunction() : () -> String")
        assertContains(generatedFile, "createTextAppendingFunction(String) : () -> String")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Relations to classes in not included packages are not shown`() {
        val compilation = newCompilation(
            DEFAULT_OPTIONS.copy(includedPackages = listOf("com.a")),
            listOf(
                SourceFile.kotlin("ClassB.kt", classInPackageBCode),
                SourceFile.kotlin("ClassA.kt", ClassInPackageACode)
            )
        )

        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContainsNot(generatedFile, "com_b_Test")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Relations to classes in excluded packages are not shown`() {
        val compilation = newCompilation(
            DEFAULT_OPTIONS.copy(excludedPackages = listOf("com.b")),
            listOf(
                SourceFile.kotlin("ClassB.kt", classInPackageBCode),
                SourceFile.kotlin("ClassA.kt", ClassInPackageACode)
            )
        )

        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContainsNot(generatedFile, "com_b_Test")
    }
}