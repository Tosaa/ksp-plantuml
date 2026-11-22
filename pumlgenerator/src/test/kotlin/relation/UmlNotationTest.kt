package relation

import CompilationTest
import Notation
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

/**
 * Verifies that the diagram is drawn in the notation of UML class diagrams with `puml.notation=UML`:
 * - Generalization (solid line, hollow triangle) between classes and between interfaces
 * - Realization (dashed line, hollow triangle) between a class or an object and an interface
 * - Association (solid line, open arrow) with a multiplicity for properties
 * - Dependency (dashed line, open arrow) for type arguments, return types and parameters
 * - Keywords for interfaces and enumerations, stereotypes for Kotlin specific modifiers
 * - Visibility of members as + - # ~
 */
class UmlNotationTest : CompilationTest() {

    private val umlOptions = DEFAULT_OPTIONS.copy(notation = Notation.UML)

    @OptIn(ExperimentalCompilerApi::class)
    private fun generate(vararg sources: SourceFile, options: Options = umlOptions): String {
        val result = newCompilation(options, sources.toList()).compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        return result.sourcesGeneratedBySymbolProcessor.first().readText()
    }

    private fun generate(code: String, options: Options = umlOptions): String = generate(SourceFile.kotlin("Code.kt", code), options = options)

    @Test
    fun `Interface extension is a generalization while implementation is a realization`() {
        val generatedFile = generate(NotationTestCode.shapes)
        // interface extends interface -> solid generalization
        assertContains(generatedFile, "shapes_Shape <|-- shapes_Polygon")
        // class/object implements interface -> dashed realization
        assertContains(generatedFile, "shapes_Polygon <|.. shapes_Square")
        assertContains(generatedFile, "shapes_Shape <|.. shapes_UnitCircle")
        assertContainsNot(generatedFile, "shapes_Polygon <|-- shapes_Square")
        assertContainsNot(generatedFile, "shapes_Shape <|-- shapes_UnitCircle")
    }

    @Test
    fun `Inheritance is drawn for classes without a package`() {
        val generatedFile = generate(NotationTestCode.rooms)
        assertContains(generatedFile, "Room <|.. MagicRoom")
    }

    @Test
    fun `Properties are associations with a multiplicity`() {
        val generatedFile = generate(NotationTestCode.car)
        assertContains(generatedFile, "city_Car --> \"1\" city_Engine")
        // nullable property
        assertContains(generatedFile, "city_Car --> \"0..1\" city_Radio")
        // collections and arrays
        assertContains(generatedFile, "city_Car --> \"*\" city_Wheel")
        assertContains(generatedFile, "city_Car --> \"*\" city_Seat")
    }

    @Test
    fun `Type arguments and return types are dependencies`() {
        val generatedFile = generate(NotationTestCode.car)
        // type argument of a type that is not a collection
        assertContains(generatedFile, "city_Car ..> city_Driver")
        // return type of a function
        assertContains(generatedFile, "city_Car ..> city_Horn")
    }

    @Test
    fun `Parameters are dependencies`() {
        val generatedFile = generate(NotationTestCode.listeners, umlOptions.copy(showParameterRelations = true))
        assertContains(generatedFile, "listeners_EventListener ..> listeners_Event")
        // type argument of a parameter
        assertContains(generatedFile, "listeners_EventListener ..> listeners_Batch")
    }

    @Test
    fun `Aggregation and composition are not used`() {
        val generatedFile = generate(NotationTestCode.car) + generate(NotationTestCode.family)
        assertContainsNot(generatedFile, "*--")
        assertContainsNot(generatedFile, "o--")
        assertContainsNot(generatedFile, "--*")
        assertContainsNot(generatedFile, "--o")
    }

    @Test
    fun `Classes that refer to each other are connected by one association for each direction`() {
        val generatedFile = generate(NotationTestCode.family)
        assertContains(generatedFile, "family_Parent -> \"*\" family_Child")
        assertContains(generatedFile, "family_Child -> \"1\" family_Parent")
    }

    @Test
    fun `Interfaces enumerations and annotations are marked with their keyword`() {
        val generatedFile = generate(NotationTestCode.members)
        assertContains(generatedFile, "interface \"Sensor\" as members_Sensor <<interface>>")
        assertContains(generatedFile, "enum \"Level\" as members_Level <<enumeration>>")
        assertContains(generatedFile, "annotation \"Experimental\" as members_Experimental <<annotation>>")
    }

    @Test
    fun `Operations name their parameters and Kotlin modifiers are stereotypes`() {
        val generatedFile = generate(NotationTestCode.members)
        assertContains(generatedFile, "+ <<suspend>> read(level : Level, retries : Int) : Int")
        // a vararg parameter accepts any number of values
        assertContains(generatedFile, "+ log(lines : String [*]) : Unit")
        assertContains(generatedFile, "+ <<ext>> reset(force : Boolean) : Unit")
        assertContains(generatedFile, "+ <<ext>> label : String")
    }

    @Test
    fun `Visibility is written with the characters of UML`() {
        val generatedFile = generate(SourceFile.kotlin("Code.kt", NotationTestCode.members), SourceFile.java("Valve.java", NotationTestCode.membersJava))
        assertContains(generatedFile, "+ name : String")
        // internal is the visibility of a module, which is closest to the visibility of a package
        assertContains(generatedFile, "~ id : Int")
        assertContains(generatedFile, "# secret : Int")
        assertContains(generatedFile, "- hidden : Int")
        // package private
        assertContains(generatedFile, "~ pressure : Int")
        assertContains(generatedFile, "~ open() : Unit")
    }

    @Test
    fun `The diagram starts with the settings for the UML notation and the prefix can override them`() {
        val generatedFile = generate(NotationTestCode.members, umlOptions.copy(prefix = "show circle"))
        val lines = generatedFile.lines()
        assertEquals("@startuml", lines[0])
        assertEquals("hide circle", lines[1])
        assertEquals("skinparam classAttributeIconSize 0", lines[2])
        assertTrue(lines.indexOf("show circle") > lines.indexOf("hide circle"), "The prefix has to follow the settings to be able to override them")
    }
}
