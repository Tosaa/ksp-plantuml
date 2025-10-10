import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.validate
import java.io.File
import java.io.OutputStreamWriter

class PumlProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger,
    val options: Options,
    val diagramCollection: ClassDiagramDescription = ClassDiagramDescription(options, logger)
) : SymbolProcessor {
    override fun finish() {
        val finalDiagram = generateFinalDiagram(diagramCollection)
        logger.i {
            """
finish():
$finalDiagram            
        """
        }
        val outputFile = if (options.outputFileName.isEmpty()) {
            File("generated/puml/ClassDiagram${finalDiagram.hashCode()}")
        } else {
            File(options.outputFileName)
        }
        saveDiagramToFile(finalDiagram, outputFile)
        super.finish()
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val files = resolver.getAllFiles()
        logger.i { "Applied options: $options" }
        logger.i { "Process Files: ${files.joinToString()}" }
        val invalidFiles = files.mapNotNull {
            it.accept(UMLGenerator(logger, diagramCollection, options), Unit)

            if (!it.validate()) {
                it
            } else {
                null
            }
        }
        logger.i { "process(): processed all ${files.toSet().size} Files" }
        return invalidFiles.toList()
    }

    private fun generateFinalDiagram(diagramCollection: ClassDiagramDescription): String {
        val diagramBuilder = StringBuilder()
        diagramBuilder.appendLine("@startuml")
        options.title.takeIf { it.isNotBlank() }?.let { diagramBuilder.appendLine("title $it") }
        options.prefix.takeIf { it.isNotBlank() }?.let { diagramBuilder.appendLine(it) }
        diagramBuilder.appendLine(diagramCollection.computeUMLClassDiagrams())
        diagramBuilder.appendLine()
        diagramBuilder.appendLine(diagramCollection.computeAllRelations())
        options.postfix.takeIf { it.isNotBlank() }?.let { diagramBuilder.appendLine(it) }
        diagramBuilder.appendLine("@enduml")
        return diagramBuilder.toString()
    }

    private fun saveDiagramToFile(fileContent: String, outputFile: File) {
        kotlin.runCatching {
            codeGenerator.generatedFile.find { it.path == outputFile.path }?.delete()

            val file = codeGenerator.createNewFileByPath(Dependencies(true), outputFile.path.trimEnd(*".puml".toCharArray()), "puml").let {
                OutputStreamWriter(it)
            }
            // val file = OutputStreamWriter(codeGenerator.createNewFile(Dependencies(true), "", "ClassDiagram", "puml"))
            file.append(fileContent)
            file.close()
            logger.i { "Diagram saved to: ${outputFile.absolutePath}" }
        }.onFailure {
            logger.e { "Failed to save diagram:\n${it.stackTraceToString()}" }
        }
    }
}

class PumlProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val unexpectedKeys = environment.options.filter { it.key !in OptionConstants.IDENTIFIER }.toList()
        environment.logger.i {
            """
KSP Environment
    platforms: ${environment.platforms}
    apiVersion: ${environment.apiVersion}            
    compilerVersion: ${environment.compilerVersion}            
    kotlinVersion: ${environment.kotlinVersion}
    kspVersion: ${environment.kspVersion}            
"""
        }
        if (unexpectedKeys.isNotEmpty()) {
            environment.logger.w { "Environment configuration contains unexpected keys: ${unexpectedKeys.joinToString()}" }
        }
        return PumlProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            options = Options(
                kspProcessorOptions = environment.options
            )
        )
    }
}

