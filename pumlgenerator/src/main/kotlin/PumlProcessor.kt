import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.validate
import java.io.OutputStreamWriter

class PumlProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger,
    val options: Options,
    val diagramCollection: ClassDiagramDescription = ClassDiagramDescription(options)
) : SymbolProcessor {
    override fun finish() {
        saveDiagramToFile(diagramCollection)
        super.finish()
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val files = resolver.getAllFiles()
        logger.i { "Process Files: ${files.joinToString()}" }
        val invalidFiles = files.filter { !it.validate() }
        files.forEach {
            it.accept(UMLGenerator(logger, diagramCollection, options), Unit)
        }
        logger.i { diagramCollection.computeUMLClassDiagrams(options) }
        logger.i { diagramCollection.computeHierarchies() }
        return invalidFiles.toList()
    }

    private fun saveDiagramToFile(diagramCollection: ClassDiagramDescription) {
        kotlin.runCatching {
            codeGenerator.generatedFile.forEach {
                it.delete()
            }
            val file = OutputStreamWriter(codeGenerator.createNewFile(Dependencies(true), "", "ClassDiagram", "puml"))
            file.appendLine("@startuml")
            file.appendLine(diagramCollection.computeUMLClassDiagrams(options))
            file.appendLine()
            if (options.showInheritance) {
                file.appendLine(diagramCollection.computeHierarchies())
            }
            file.appendLine("@enduml")
            file.close()
        }
    }
}

class PumlProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return PumlProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            options = Options(
                kspProcessorOptions = environment.options
            )
        )
    }
}

