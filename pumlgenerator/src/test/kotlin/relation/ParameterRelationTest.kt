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

class ParameterRelationTest : CompilationTest() {

    private val eventCode = """
    package events
    class Event
    """

    private val listenerCode = """
    package events
    interface EventListener {
        fun onEvent(event: Event)
    }
    """

    private val batchListenerCode = """
    package events
    interface BatchListener {
        fun onEvents(events: List<Event>)
    }
    """

    private val eventSourceCode = """
    package events
    interface EventSource {
        fun subscribe(callback: (Event) -> Unit)
    }
    """

    private val chainCode = """
    package events
    interface Chain {
        fun then(next: Chain)
    }
    """

    private val hiddenEventCode = """
    package events.hidden
    class HiddenEvent
    """

    private val hiddenListenerCode = """
    package events
    import events.hidden.HiddenEvent
    interface HiddenListener {
        fun onEvent(event: HiddenEvent)
    }
    """

    private val withParameterRelations = Options(showParameterRelations = true)

    @OptIn(ExperimentalCompilerApi::class)
    private fun generate(options: Options, vararg codes: String): String {
        val files = codes.mapIndexed { index, code -> SourceFile.kotlin("Code$index.kt", code) }
        val result = newCompilation(options, files).compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        return result.sourcesGeneratedBySymbolProcessor.first().readText()
    }

    @Test
    fun `Relations of parameters are not created by default`() {
        val generatedFile = generate(Options(), eventCode, listenerCode, batchListenerCode, eventSourceCode)
        assertContains(generatedFile, "onEvent(Event) : Unit")
        assertContainsNot(generatedFile, "events_EventListener --> events_Event")
        assertContainsNot(generatedFile, "events_BatchListener ..> events_Event")
        assertContainsNot(generatedFile, "events_EventSource ..> events_Event")
    }

    @Test
    fun `Create relations of parameters between classes`() {
        val generatedFile = generate(withParameterRelations, eventCode, listenerCode)
        assertContains(generatedFile, "events_EventListener --> events_Event")
    }

    @Test
    fun `Relations of parameters do not depend on the relations of functions`() {
        val generatedFile = generate(withParameterRelations.copy(showFunctionRelations = false), eventCode, listenerCode)
        assertContains(generatedFile, "events_EventListener --> events_Event")
    }

    @Test
    fun `Create indirect relations if a List is passed`() {
        val generatedFile = generate(withParameterRelations, eventCode, batchListenerCode)
        assertContains(generatedFile, "onEvents(List<Event>) : Unit")
        assertContains(generatedFile, "events_BatchListener ..> events_Event")
    }

    @Test
    fun `Create indirect relations if a function is passed`() {
        val generatedFile = generate(withParameterRelations, eventCode, eventSourceCode)
        assertContains(generatedFile, "subscribe((Event) -> Unit) : Unit")
        assertContains(generatedFile, "events_EventSource ..> events_Event")
    }

    @Test
    fun `Do not create indirect relations if disabled`() {
        val generatedFile = generate(withParameterRelations.copy(showIndirectRelations = false), eventCode, listenerCode, batchListenerCode, eventSourceCode)
        assertContains(generatedFile, "events_EventListener --> events_Event")
        assertContainsNot(generatedFile, "events_BatchListener ..> events_Event")
        assertContainsNot(generatedFile, "events_EventSource ..> events_Event")
    }

    @Test
    fun `Do not create a relation to itself`() {
        val generatedFile = generate(withParameterRelations, chainCode)
        assertContains(generatedFile, "then(Chain) : Unit")
        assertContainsNot(generatedFile, "events_Chain --> events_Chain")
    }

    @Test
    fun `Do not create a relation if the type is not part of the rendered classes`() {
        val generatedFile = generate(withParameterRelations.copy(excludedPackages = listOf("events.hidden")), hiddenEventCode, hiddenListenerCode)
        assertContains(generatedFile, "onEvent(HiddenEvent) : Unit")
        assertContainsNot(generatedFile, "events_hidden_HiddenEvent")
    }
}
