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

/**
 * Verifies that the diagram keeps the notation of the earlier versions by default, which relies on the defaults of plantuml:
 * - Inheritance (solid line, hollow triangle) for extending a class and for implementing an interface
 * - Solid lines that end with a diamond for properties, dotted ones for type arguments, collections and arrays
 * - Solid lines that end with an arrow for return types, dotted ones for type arguments
 * - Circled characters instead of keywords for interfaces and enumerations
 * - Visibility of members as icons, functions with the types of their parameters
 */
class PlantumlNotationTest : CompilationTest() {

    @OptIn(ExperimentalCompilerApi::class)
    private fun generate(vararg sources: SourceFile, options: Options = DEFAULT_OPTIONS): String {
        val result = newCompilation(options, sources.toList()).compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        return result.sourcesGeneratedBySymbolProcessor.first().readText()
    }

    private fun generate(code: String): String = generate(SourceFile.kotlin("Code.kt", code))

    @Test
    fun `Implementing an interface is drawn like extending a class`() {
        val generatedFile = generate(NotationTestCode.shapes)
        assertContains(generatedFile, "shapes_Shape <|-- shapes_Polygon")
        assertContains(generatedFile, "shapes_Polygon <|-- shapes_Square")
        assertContains(generatedFile, "shapes_Shape <|-- shapes_UnitCircle")
        assertContainsNot(generatedFile, "<|..")
    }

    @Test
    fun `Inheritance is drawn for classes without a package`() {
        val generatedFile = generate(NotationTestCode.rooms)
        assertContains(generatedFile, "Room <|-- MagicRoom")
    }

    @Test
    fun `Properties end with a diamond`() {
        val generatedFile = generate(NotationTestCode.car)
        assertContains(generatedFile, "city_Car --* city_Engine")
        assertContains(generatedFile, "city_Car --* city_Radio")
        // collections, arrays and type arguments
        assertContains(generatedFile, "city_Car ..* city_Wheel")
        assertContains(generatedFile, "city_Car ..* city_Seat")
        assertContains(generatedFile, "city_Car ..* city_Driver")
        // no multiplicities
        assertContainsNot(generatedFile, "\"1\"")
        assertContainsNot(generatedFile, "\"0..1\"")
        assertContainsNot(generatedFile, "\"*\"")
    }

    @Test
    fun `Return types end with an arrow`() {
        val generatedFile = generate(NotationTestCode.car)
        assertContains(generatedFile, "city_Car --> city_Horn")
    }

    @Test
    fun `Classes that refer to each other are drawn on the same level`() {
        val generatedFile = generate(NotationTestCode.family)
        assertContains(generatedFile, "family_Parent .* family_Child")
        assertContains(generatedFile, "family_Child -* family_Parent")
    }

    @Test
    fun `Interfaces enumerations and annotations have no keyword, since plantuml draws their kind as circled character`() {
        val generatedFile = generate(NotationTestCode.members)
        assertContains(generatedFile, "interface \"Sensor\" as members_Sensor {")
        assertContains(generatedFile, "enum \"Level\" as members_Level {")
        assertContains(generatedFile, "annotation \"Experimental\" as members_Experimental {")
        assertContainsNot(generatedFile, "<<interface>>")
        assertContainsNot(generatedFile, "<<enumeration>>")
        assertContainsNot(generatedFile, "<<annotation>>")
    }

    @Test
    fun `Operations list the types of their parameters`() {
        val generatedFile = generate(NotationTestCode.members)
        assertContains(generatedFile, "+ suspend read(Level, Int) : Int")
        assertContains(generatedFile, "+ log(String) : Unit")
        assertContains(generatedFile, "+ <ext> reset(Boolean) : Unit")
        assertContains(generatedFile, "+ <ext> label : String")
    }

    @Test
    fun `Internal and package private members are drawn as protected`() {
        val generatedFile = generate(SourceFile.kotlin("Code.kt", NotationTestCode.members), SourceFile.java("Valve.java", NotationTestCode.membersJava))
        assertContains(generatedFile, "+ name : String")
        assertContains(generatedFile, "# id : Int")
        assertContains(generatedFile, "# secret : Int")
        assertContains(generatedFile, "- hidden : Int")
        assertContains(generatedFile, "# pressure : Int")
        assertContains(generatedFile, "# open() : Unit")
        assertContainsNot(generatedFile, "~ ")
    }

    @Test
    fun `The diagram starts without settings for the notation`() {
        val generatedFile = generate(NotationTestCode.members)
        assertEquals("@startuml", generatedFile.lines()[0])
        assertContainsNot(generatedFile, "hide circle")
        assertContainsNot(generatedFile, "skinparam classAttributeIconSize")
    }
}
