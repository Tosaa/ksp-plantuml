import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OptionsTest : CompilationTest() {

    private fun kotlinClassCode(visibilityModifier: String, className: String): SourceFile = SourceFile.kotlin(
        "$className.kt",
        """
            $visibilityModifier class $className{
                val one:Int = 4
                public val two:Int = 4
                internal val three:Int = 4
                private val four:Int = 4
                fun firstFun():Unit{}
                public fun secondFun():Unit{}
                internal fun thirdFun():Unit{}
                private fun fourthFun():Unit{}
            }
        """.trimIndent()
    )

    @OptIn(ExperimentalCompilerApi::class)
    fun compile(options: Options, sources: List<SourceFile>): String {
        val compilation = newCompilation(options, sources = sources)
        compilation.compile()
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        return result.sourcesGeneratedBySymbolProcessor.first().readText()
    }

    @Test
    fun `Only public properties and functions are visualized`() {
        val puml = compile(
            Options(
                showPublicClasses = true, showPublicFunctions = true, showPublicProperties = true,
                showInternalClasses = false, showInternalFunctions = false, showInternalProperties = false,
                showPrivateClasses = false, showPrivateFunctions = false, showPrivateProperties = false,
            ), listOf(kotlinClassCode("public", "TestClass"))
        )
        // Public
        assertContains(puml, "one : Int")
        assertContains(puml, "two : Int")
        assertContains(puml, "firstFun() : Unit")
        assertContains(puml, "secondFun() : Unit")
        // Internal
        assertContainsNot(puml, "three : Int")
        assertContainsNot(puml, "thirdFun() : Unit")
        // Private
        assertContainsNot(puml, "four : Int")
        assertContainsNot(puml, "fourthFun() : Unit")
    }

    @Test
    fun `Only internal properties and functions are visualized`() {
        val puml = compile(
            Options(
                showPublicClasses = false, showPublicFunctions = false, showPublicProperties = false,
                showInternalClasses = true, showInternalFunctions = true, showInternalProperties = true,
                showPrivateClasses = false, showPrivateFunctions = false, showPrivateProperties = false,
            ), listOf(kotlinClassCode("internal", "TestClass"))
        )
        // Public
        assertContainsNot(puml, "one : Int")
        assertContainsNot(puml, "two : Int")
        assertContainsNot(puml, "firstFun() : Unit")
        assertContainsNot(puml, "secondFun() : Unit")
        // Internal
        assertContains(puml, "three : Int")
        assertContains(puml, "thirdFun() : Unit")
        // Private
        assertContainsNot(puml, "four : Int")
        assertContainsNot(puml, "fourthFun() : Unit")
    }

    @Test
    fun `Only private properties and functions are visualized`() {
        val puml = compile(
            Options(
                showPublicClasses = false, showPublicFunctions = false, showPublicProperties = false,
                showInternalClasses = false, showInternalFunctions = false, showInternalProperties = false,
                showPrivateClasses = true, showPrivateFunctions = true, showPrivateProperties = true,
            ), listOf(kotlinClassCode("private", "TestClass"))
        )
        // Public
        assertContainsNot(puml, "one : Int")
        assertContainsNot(puml, "two : Int")
        assertContainsNot(puml, "firstFun() : Unit")
        assertContainsNot(puml, "secondFun() : Unit")
        // Internal
        assertContainsNot(puml, "three : Int")
        assertContainsNot(puml, "thirdFun() : Unit")
        // Private
        assertContains(puml, "four : Int")
        assertContains(puml, "fourthFun() : Unit")
    }

    @Test
    fun `public and Internal properties and functions are visualized`() {
        val puml = compile(
            Options(
                showPublicClasses = true, showPublicFunctions = true, showPublicProperties = true,
                showInternalClasses = true, showInternalFunctions = true, showInternalProperties = true,
                showPrivateClasses = false, showPrivateFunctions = false, showPrivateProperties = false
            ), listOf(kotlinClassCode("public", "TestClass"))
        )
        // Public
        assertContains(puml, "one : Int")
        assertContains(puml, "two : Int")
        assertContains(puml, "firstFun() : Unit")
        assertContains(puml, "secondFun() : Unit")
        // Internal
        assertContains(puml, "three : Int")
        assertContains(puml, "thirdFun() : Unit")
        // Private
        assertContainsNot(puml, "four : Int")
        assertContainsNot(puml, "fourthFun() : Unit")
    }

    @Test
    fun `visibility modifiers can be shown`() {
        val puml = compile(
            Options(
                showPublicClasses = true, showPublicFunctions = true, showPublicProperties = true,
                showInternalClasses = true, showInternalFunctions = true, showInternalProperties = true,
                showPrivateClasses = true, showPrivateFunctions = true, showPrivateProperties = true,
                showVisibilityModifiers = true
            ), listOf(kotlinClassCode("public", "TestClass"))
        )
        // Public
        assertContains(puml, "+ one : Int")
        assertContains(puml, "+ two : Int")
        assertContains(puml, "+ firstFun() : Unit")
        assertContains(puml, "+ secondFun() : Unit")
        // Internal
        assertContains(puml, "# three : Int")
        assertContains(puml, "# thirdFun() : Unit")
        // Private
        assertContains(puml, "- four : Int")
        assertContains(puml, "- fourthFun() : Unit")
    }

    @Test
    fun `visibility modifiers can be hidden`() {
        val puml = compile(
            Options(
                showPublicClasses = true, showPublicFunctions = true, showPublicProperties = true,
                showInternalClasses = true, showInternalFunctions = true, showInternalProperties = true,
                showPrivateClasses = true, showPrivateFunctions = true, showPrivateProperties = true,
                showVisibilityModifiers = false
            ), listOf(kotlinClassCode("public", "TestClass"))
        )
        // Public
        assertContainsNot(puml, "+ one : Int")
        assertContains(puml, "one : Int")
        assertContainsNot(puml, "+ two : Int")
        assertContains(puml, "two : Int")
        assertContainsNot(puml, "+ firstFun() : Unit")
        assertContains(puml, "firstFun() : Unit")
        assertContainsNot(puml, "+ secondFun() : Unit")
        assertContains(puml, "secondFun() : Unit")
        // Internal
        assertContainsNot(puml, "# three : Int")
        assertContains(puml, "three : Int")
        assertContainsNot(puml, "# thirdFun() : Unit")
        assertContains(puml, "thirdFun() : Unit")
        // Private
        assertContainsNot(puml, "- four : Int")
        assertContains(puml, "four : Int")
        assertContainsNot(puml, "- fourthFun() : Unit")
        assertContains(puml, "fourthFun() : Unit")
    }

    @Test
    fun `Only public classes are visualized`() {
        val puml = compile(
            Options(showPublicClasses = true, showPublicFunctions = true, showPublicProperties = true,
                showInternalClasses = false, showInternalFunctions = true, showInternalProperties = true,
                showPrivateClasses = false, showPrivateFunctions = true, showPrivateProperties = true,), listOf(
                kotlinClassCode("public", "FirstClass"),
                kotlinClassCode("internal", "SecondClass"),
                kotlinClassCode("public", "ThirdClass"),
            )
        )
        assertContains(puml, "FirstClass")
        assertContainsNot(puml, "SecondClass")
        assertContains(puml, "ThirdClass")
    }

    @Test
    fun `title and prefix and postfix are appended`() {
        val puml = compile(
            Options(title = "Diagram without anything", prefix = "'start", postfix = "'end"), listOf()
        )
        assertContains(puml, "title Diagram without anything")
        assertContains(puml, "'start")
        assertContains(puml, "'end")
    }
}