import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import uml.element.DiagramElement
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExtensionFunctionsTest : CompilationTest() {

    val colorClassWithExtensionsCode = """
    package my.color
    class Color(val hex:String) {
        companion object {
            val red : Color
                get() = Color("ffff0000")
            fun halfTransparent(originalColor:Color) : Color = Color("7f"+originalColor.hex.takeLast(6))
        }
    }

    fun Color.fullTransparent() : Color = Color("00" + this.hex.takeLast(6))
    fun Color.Companion.fullTransparent(originalColor: Color) : Color = Color("00" + originalColor.hex.takeLast(6))
    """

    val colorClassCode = """
    package my.color
    class Color(val hex:String){
        companion object
    }
    """

    val colorExtensionFunctionsCode = """
    package my.color
    
    // companion
    val Color.Companion.red : Color
        get() = Color("ffff0000")
    // Not companion
    fun Color.halfTransparent() : Color = Color("7f"+this.hex.takeLast(6))
    """


    val animalInterfaceCode = """
    package explorer.database
    public interface Animal {
        val name : String
        val isExtinct : Boolean
        fun makeSound() : String
    }
    """

    val animalExtensionCode = """
    package explorer.database.extension
    import explorer.database.Animal
    public val Animal.isAlive : Boolean
        get() = !this.isExtinct

    public fun Animal.describe() : String = "${'\$'}name is an animal that makes ${'\$'}{makeSound()}}"
    """

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Extension function declared in other file is marked as such`() {
        val compilation = newCompilation(DEFAULT_OPTIONS.copy(markExtensions = true), listOf(SourceFile.kotlin("Color.kt", colorClassCode), SourceFile.kotlin("ColorExtensions.kt", colorExtensionFunctionsCode)))
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "{static} <ext> red : Color")
        assertContains(generatedFile, "<ext> halfTransparent() : Color")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Extension function declared in companion object is marked as such`() {
        val compilation = newCompilation(DEFAULT_OPTIONS.copy(markExtensions = true), listOf(SourceFile.kotlin("Color.kt", colorClassWithExtensionsCode)))
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContainsNot(generatedFile, "The following relations were added to the graph but are invalid")
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "{static} red : Color")
        assertContains(generatedFile, "{static} halfTransparent(Color) : Color")
        assertContains(generatedFile, "<ext> fullTransparent() : Color")
        assertContains(generatedFile, "{static} <ext> fullTransparent(Color) : Color")
    }


    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Extensions are shown as well`() {
        val files = listOf(
            SourceFile.kotlin("Animal.kt", animalInterfaceCode),
            SourceFile.kotlin("AnimalExtension.kt", animalExtensionCode)
        )
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, "isAlive : Boolean")
        assertContains(generatedFile, "describe() : String")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Extensions for primitive types works`() {
        val files = listOf(
            SourceFile.kotlin(
                "StringExtension.kt", """
package explorer.database            
public fun String.withPrefix(prefix:String): String = "prefix$this"
""".trimIndent()
            )
        )
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, DiagramElement.shellString)
        assertContains(generatedFile, "class \"String\" as kotlin_String")
        assertContains(generatedFile, "<ext> withPrefix(String) : String")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Extension variable for classes in external packages works`() {
        val files = listOf(
            SourceFile.kotlin(
                "RegexExtension.kt", """
package explorer.database
import kotlin.text.Regex
val Regex.hasSomething :Boolean
    get() = this.pattern.isNotEmpty()
""".trimIndent()
            )
        )
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, DiagramElement.shellString)
        assertContains(generatedFile, "class \"Regex\" as kotlin_text_Regex")
        assertContains(generatedFile, "<ext> hasSomething : Boolean")
        assertContainsNot(generatedFile, "matches") // Regex should be just a shell and not contain any normal variables / functions
        assertContainsNot(generatedFile, "pattern : String") // Regex should be just a shell and not contain any normal variables / functions
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Extension function for classes in external packages works`() {
        val files = listOf(
            SourceFile.kotlin(
                "RegexExtension.kt", """
package explorer.database
import kotlin.text.Regex
fun Regex.hasSomething() : Boolean = this.pattern.isNotEmpty()
""".trimIndent()
            )
        )
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, DiagramElement.shellString)
        assertContains(generatedFile, "class \"Regex\" as kotlin_text_Regex")
        assertContains(generatedFile, "<ext> hasSomething() : Boolean")
        assertContainsNot(generatedFile, "matches") // Regex should be just a shell and not contain any normal variables / functions
        assertContainsNot(generatedFile, "pattern : String") // Regex should be just a shell and not contain any normal variables / functions
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Extension variable for companion objects of classes in external packages works`() {
        val files = listOf(
            SourceFile.kotlin(
                "RegexExtension.kt", """
package explorer.database
import kotlin.text.Regex
val Regex.Companion.startsWithFoo : Regex 
    get () = Regex("Foo.*")
""".trimIndent()
            )
        )
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, DiagramElement.shellString)
        assertContains(generatedFile, "class \"Regex\" as kotlin_text_Regex")
        assertContains(generatedFile, "{static} <ext> startsWithFoo : Regex")
        assertContainsNot(generatedFile, "matches") // Regex should be just a shell and not contain any normal variables / functions
        assertContainsNot(generatedFile, "pattern : String") // Regex should be just a shell and not contain any normal variables / functions
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Extension function for companion objects of classes in external packages works`() {
        val files = listOf(
            SourceFile.kotlin(
                "RegexExtension.kt", """
package explorer.database
import kotlin.text.Regex
fun Regex.Companion.hasSomething() : Boolean = false
""".trimIndent()
            )
        )
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, DiagramElement.shellString)
        assertContains(generatedFile, "class \"Regex\" as kotlin_text_Regex")
        assertContains(generatedFile, "{static} <ext> hasSomething() : Boolean")
        assertContainsNot(generatedFile, "matches") // Regex should be just a shell and not contain any normal variables / functions
        assertContainsNot(generatedFile, "pattern : String") // Regex should be just a shell and not contain any normal variables / functions
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Extension function for File works`() {
        val files = listOf(
            SourceFile.kotlin(
                "RegexExtension.kt", """
package plantuml.utils
import java.io.File

data class PlantumlTemplate(val content: String)

fun File.writePlantuml(plantumlTemplate: PlantumlTemplate): Unit {
    this.writeText("startuml\n" + plantumlTemplate.content + "\nenduml")
}

val File.isPlantumlDiagram: Boolean
    get() = this.isFile && this.exists() && listOf("startuml", "enduml").all { it in this.readText() }
    
""".trimIndent()
            )
        )
        val compilation = newCompilation(DEFAULT_OPTIONS, files)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, DiagramElement.shellString)
        assertContains(generatedFile, "class \"File\" as java_io_File")
        assertContains(generatedFile, "<ext> writePlantuml(PlantumlTemplate) : Unit")
        assertContains(generatedFile, "<ext> isPlantumlDiagram : Boolean")
    }
}