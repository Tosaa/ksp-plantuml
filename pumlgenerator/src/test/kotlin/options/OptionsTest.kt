package options

import CompilationTest
import Options
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.assertEquals
import kotlin.test.assertTrue

open class OptionsTest : CompilationTest() {

    fun kotlinClassCode(visibilityModifier: String, className: String, packagePrefix: String = ""): SourceFile = SourceFile.kotlin(
        "$className.kt",
        """
            $packagePrefix
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
}