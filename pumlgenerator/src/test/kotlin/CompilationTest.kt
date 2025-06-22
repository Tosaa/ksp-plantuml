import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspProcessorOptions
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import com.tschuchort.compiletesting.symbolProcessorProviders
import com.tschuchort.compiletesting.useKsp2
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.incremental.createDirectory
import org.jetbrains.kotlin.ir.types.IdSignatureValues.result
import java.io.File
import kotlin.test.assertFalse

open class CompilationTest {
    val DEFAULT_OPTIONS = Options()

    @OptIn(ExperimentalCompilerApi::class)
    fun newCompilation(options: Options, sources: List<SourceFile>) = KotlinCompilation().apply {
        useKsp2()
        inheritClassPath = true
        this.sources = sources
        kspProcessorOptions.putAll(options.asMap())
        symbolProcessorProviders += PumlProcessorProvider()
    }

    fun saveUMLInDocs(generatedFile: File, name: String) {
        val docsFolder = File("../doc/plantuml").apply { createDirectory() }
        val copiedFile = File(docsFolder, name.ensureEndsWith(".puml")).apply { createNewFile() }
        generatedFile.copyTo(copiedFile, overwrite = true)
    }

}

internal fun assertContainsNot(charSequence: CharSequence, other: CharSequence, ignoreCase: Boolean = false, message: String? = null) {
    assertFalse { charSequence.contains(other, ignoreCase = ignoreCase) }
}
