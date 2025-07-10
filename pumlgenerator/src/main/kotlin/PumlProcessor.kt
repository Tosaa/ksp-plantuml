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
        saveDiagramToFile(finalDiagram)
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
        diagramBuilder.appendLine(diagramCollection.computeUMLClassDiagrams(options))
        diagramBuilder.appendLine()
        if (options.showInheritance) {
            diagramBuilder.appendLine("'Inheritance relations")
            diagramBuilder.appendLine(diagramCollection.computeInheritanceRelations())
        }
        if (options.showPropertyRelations) {
            diagramBuilder.appendLine("'Property relations")
            diagramBuilder.appendLine(diagramCollection.computePropertyRelations())
        }
        if (options.showFunctionRelations) {
            diagramBuilder.appendLine("'Function relations")
            diagramBuilder.appendLine(diagramCollection.computeFunctionRelations())
        }
        options.postfix.takeIf { it.isNotBlank() }?.let { diagramBuilder.appendLine(it) }
        diagramBuilder.appendLine("@enduml")
        return diagramBuilder.toString()
    }

    private fun saveDiagramToFile(fileContent: String) {
        kotlin.runCatching {
            codeGenerator.generatedFile.forEach {
                it.delete()
            }

            val file = codeGenerator.createNewFileByPath(Dependencies(true), File("generated/puml/ClassDiagram${fileContent.hashCode()}").path, "puml").let {
                OutputStreamWriter(it)
            }
            // val file = OutputStreamWriter(codeGenerator.createNewFile(Dependencies(true), "", "ClassDiagram", "puml"))
            file.append(fileContent)
            file.close()
        }
    }
}

class PumlProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val unexpectedKeys = environment.options.filter { it.key !in ALL_KEYS }.toList()
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

