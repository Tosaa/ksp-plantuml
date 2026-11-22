package options

import Notation
import Options
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.kspProcessorOptions
import com.tschuchort.compiletesting.symbolProcessorProviders
import com.tschuchort.compiletesting.useKsp2
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OptionComputationTest {

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `KSPProcessor options are passed to Custom Processor Options`() {
        var options: Options? = null
        val compilation = KotlinCompilation().apply {
            useKsp2()
            inheritClassPath = true
            kspProcessorOptions["puml.excludedPackages"] = "foo.bar,z.b.x.y"
            kspProcessorOptions["puml.excludedClassNamePrefixes"] = "Hilt_, Fake"
            kspProcessorOptions["puml.excludedClassNameSuffixes"] = "_Impl"
            kspProcessorOptions["puml.excludedClassNameContains"] = "Generated"
            kspProcessorOptions["puml.filterPreset"] = "room"
            kspProcessorOptions["puml.showJavaObjectMembers"] = "true"
            kspProcessorOptions["puml.showGeneratedMembers"] = "true"
            kspProcessorOptions["puml.notation"] = "uml"
            kspProcessorOptions["puml.showParameterRelations"] = "true"
            symbolProcessorProviders += SymbolProcessorProvider { env ->
                options = Options(env.options)
                return@SymbolProcessorProvider object : SymbolProcessor {
                    override fun process(resolver: Resolver): List<KSAnnotated> {
                        return emptyList()
                    }
                }
            }
        }
        compilation.compile()
        assertEquals(listOf("foo.bar", "z.b.x.y"), options?.excludedPackages)
        assertEquals(Options.DEFAULT_EXCLUDED_FUNCTIONS, options?.excludedFunctionNames)
        assertEquals(listOf("Hilt_", "Fake"), options?.excludedClassNamePrefixes)
        assertEquals(listOf("_Impl"), options?.excludedClassNameSuffixes)
        assertEquals(listOf("Generated"), options?.excludedClassNameContains)
        assertEquals("room", options?.filterPreset)
        assertEquals(true, options?.showJavaObjectMembers)
        assertEquals(true, options?.showGeneratedMembers)
        assertEquals(Notation.UML, options?.notation)
        assertEquals(true, options?.showParameterRelations)
        // Todo: further Options resolving
    }

    @Test
    fun `Options that are not passed keep their default value`() {
        val options = Options(emptyMap())
        assertEquals(Options.DEFAULT_FILTER_PRESET, options.filterPreset)
        assertEquals(false, options.showJavaObjectMembers)
        assertEquals(false, options.showGeneratedMembers)
        assertEquals(false, options.showParameterRelations)
        assertEquals(true, options.saveLogToFile)
        assertEquals(Notation.PLANTUML, options.notation)
    }

    @Test
    fun `The name of a notation is not case sensitive and a blank name selects the default notation`() {
        assertEquals(Notation.UML, Notation.fromName(" Uml "))
        assertEquals(Notation.PLANTUML, Notation.fromName("plantuml"))
        assertEquals(Notation.PLANTUML, Options(mapOf("puml.notation" to " ")).notation)
    }

    @Test
    fun `An unknown notation is reported when the options are created`() {
        val exception = assertFailsWith<IllegalArgumentException> { Options(mapOf("puml.notation" to "SYSML")) }
        assertContains(exception.message.orEmpty(), "puml.notation")
        assertContains(exception.message.orEmpty(), "PLANTUML, UML")
    }

}