import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate

class UMLGenerator(val logger: KSPLogger, val diagrams: ClassDiagramDescription, val options: Options) : KSVisitorVoid() {
    override fun visitFile(file: KSFile, data: Unit) {
        if (!file.validate()) {
            logger.i { "visitFile(): $file is not valid" }
            return
        }

        logger.v { "${file.fileName} -> ${file.declarations.joinToString()}" }
        file.declarations.forEach { it.accept(this, data) }
    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        if (!classDeclaration.validate()) {
            logger.i { "visitClassDeclaration(): $classDeclaration is not valid" }
            return
        }
        if (!options.isValid(classDeclaration, logger)) {
            return
        }
        logger.v { "VisitClassDeclaration of: ${classDeclaration.qualifiedName?.asString()}" }

        diagrams.addClass(classDeclaration = classDeclaration)

    }

}
