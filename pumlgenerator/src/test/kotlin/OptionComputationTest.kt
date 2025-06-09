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
import kotlin.test.assertEquals

class OptionComputationTest {

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `KSPProcessor options are passed to Custom Processor Options`() {
        var options: Options? = null
        val compilation = KotlinCompilation().apply {
            useKsp2()
            inheritClassPath = true
            kspProcessorOptions["excludedPackages"] = "foo.bar,z.b.x.y"
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
        assertEquals(Options.DEFAULT_EXCLUDED_FUNCTIONS, options?.excludedFunctions)
        // Todo: further Options resolving
    }

}