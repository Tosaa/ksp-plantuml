import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
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
    
    val Color.Companion.red : Color
        get() = Color("ffff0000")
    fun Color.halfTransparent() : Color = Color("7f"+this.hex.takeLast(6))
    """

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Extension function declared in other file is marked as such`() {
        val compilation = newCompilation(DEFAULT_OPTIONS.copy(markExtensions = true), listOf(SourceFile.kotlin("Color.kt", colorClassCode), SourceFile.kotlin("ColorExtensions.kt", colorExtensionFunctionsCode)))
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "{static} <ext> red : Color")
        assertContains(generatedFile, "{static} <ext> halfTransparent(Color) : Color")
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `Extension function declared in companion object is marked as such`() {
        val compilation = newCompilation(DEFAULT_OPTIONS.copy(markExtensions = true), listOf(SourceFile.kotlin("Color.kt", colorClassWithExtensionsCode)))
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertTrue { result.sourcesGeneratedBySymbolProcessor.toList().isNotEmpty() }
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first().readText()
        assertContains(generatedFile, "@startuml")
        assertContains(generatedFile, "@enduml")
        assertContains(generatedFile, "{static} red : Color")
        assertContains(generatedFile, "{static} halfTransparent(Color) : Color")
        assertContains(generatedFile, "<ext> fullTransparent() : Color")
        assertContains(generatedFile, "{static} <ext> fullTransparent(Color) : Color")
    }
}