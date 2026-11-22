package options

import CompilationTest
import Options
import assertContainsNot
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class MemberFilterOptionsTest : CompilationTest() {

    private val sensorJavaCode = """
    package device;
    public class Sensor {
        public int read() { return 0; }
    }
    """

    private val levelCode = """
    package device
    enum class Level { LOW, HIGH }
    """

    // Functions with the names of functions of java.lang.Object, but declared by the class itself
    private val notifierCode = """
    package device
    interface Notifier {
        fun notify(message: String)
        fun wait(seconds: Int)
        fun clone(times: Int): Notifier
    }
    """

    // A function with the name of a generated function of an enum, but declared by the enum itself
    private val modeJavaCode = """
    package game;
    public enum Mode {
        ON, OFF;
        public static Mode valueOf(int code) { return ON; }
    }
    """

    // Members with the names of generated members of an enum, but declared by the enum itself
    private val diceCode = """
    package game
    enum class Dice(val values: List<Int>) {
        D6(listOf(1, 2, 3, 4, 5, 6));

        companion object {
            fun valueOf(sides: Int): Dice = D6
        }
    }
    """

    private val dataCode = """
    package game
    data class Board(val a: Int, val b: Int, val c: Int, val d: Int, val e: Int, val f: Int, val g: Int, val h: Int, val i: Int, val j: Int)
    data class Point(val x: Int) {
        fun copy(label: String): Point = this
    }
    data object Registry {
        fun copy(): Registry = this
    }
    class Document {
        fun copy(): Document = Document()
        fun component1(): Int = 1
    }
    """

    @OptIn(ExperimentalCompilerApi::class)
    private fun generate(options: Options, vararg sources: SourceFile): String {
        val result = newCompilation(options, sources.toList()).compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        return result.sourcesGeneratedBySymbolProcessor.first().readText()
    }

    @Test
    fun `functions of java lang Object are hidden by default`() {
        val generatedFile = generate(Options(), SourceFile.java("Sensor.java", sensorJavaCode), SourceFile.kotlin("Level.kt", levelCode))
        assertContains(generatedFile, "read() : Int")
        listOf("getClass(", "notify(", "notifyAll(", "wait(", "clone(", "finalize(", "getDeclaringClass(", "describeConstable(").forEach {
            assertContainsNot(generatedFile, it)
        }
    }

    @Test
    fun `functions of java lang Object are shown with showJavaObjectMembers`() {
        val generatedFile = generate(Options(showJavaObjectMembers = true), SourceFile.java("Sensor.java", sensorJavaCode), SourceFile.kotlin("Level.kt", levelCode))
        assertContains(generatedFile, "read() : Int")
        assertContains(generatedFile, "clone() : Any")
    }

    @Test
    fun `functions that are declared by a class are not treated as functions of java lang Object`() {
        val generatedFile = generate(Options(), SourceFile.kotlin("Notifier.kt", notifierCode))
        assertContains(generatedFile, "notify(String) : Unit")
        assertContains(generatedFile, "wait(Int) : Unit")
        assertContains(generatedFile, "clone(Int) : Notifier")
    }

    @Test
    fun `members that are declared by an enum are not treated as generated members`() {
        val generatedFile = generate(Options(), SourceFile.kotlin("Dice.kt", diceCode), SourceFile.java("Mode.java", modeJavaCode))
        assertContains(generatedFile, "values : List<Int>")
        assertContains(generatedFile, "valueOf(Int) : Dice")
        assertContains(generatedFile, "valueOf(Int) : Mode")
        assertContainsNot(generatedFile, "values() : Array")
        assertContainsNot(generatedFile, "valueOf(String)")
        assertContainsNot(generatedFile, "ordinal : Int")
    }

    @Test
    fun `all component functions of a data class are hidden by default`() {
        val generatedFile = generate(Options(), SourceFile.kotlin("Data.kt", dataCode))
        assertContains(generatedFile, "class \"Board\" as game_Board <<Data>>")
        assertContainsNot(generatedFile, "component2() : Int")
        assertContainsNot(generatedFile, "component10() : Int")
        assertContainsNot(generatedFile, "copy(Int")
    }

    @Test
    fun `functions with the names of generated functions are shown for other classes`() {
        val generatedFile = generate(Options(), SourceFile.kotlin("Data.kt", dataCode))
        assertContains(generatedFile, "copy() : Registry")
        assertContains(generatedFile, "copy() : Document")
        // An overload of the generated function that is declared by the data class itself
        assertContains(generatedFile, "copy(String) : Point")
        assertContainsNot(generatedFile, "copy(Int) : Point")
        // Only the regular class declares this function itself, since the ones of the data class are hidden
        assertEquals(1, generatedFile.split("component1() : Int").size - 1, generatedFile)
    }

    @Test
    fun `generated members of a data class are shown with showGeneratedMembers`() {
        val generatedFile = generate(Options(showGeneratedMembers = true), SourceFile.kotlin("Data.kt", dataCode))
        assertContains(generatedFile, "component10() : Int")
        assertContains(generatedFile, "copy(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int) : Board")
    }
}
