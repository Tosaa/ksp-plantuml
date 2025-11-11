import com.google.devtools.ksp.findActualType
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import uml.fullQualifiedName

class UMLGenerator(val logger: KSPLogger, val diagrams: ClassDiagramDescription, val options: Options) : KSVisitorVoid() {
    override fun visitFile(file: KSFile, data: Unit) {
        if (!file.validate()) {
            logger.i { "visitFile(): $file is not valid" }
            return
        }

        logger.v { "${file.fileName} -> ${file.declarations.joinToString()}" }
        val ignoredDeclarations = file.declarations.filter { it !is KSClassDeclaration && it !is KSFunctionDeclaration && it !is KSPropertyDeclaration && it !is KSTypeAlias }.toList()
        if (ignoredDeclarations.isNotEmpty()) {
            logger.w { "The following declarations were ignored since only class declarations are considered: ${ignoredDeclarations.joinToString()}" }
        }
        file.declarations.forEach { it.accept(this, data) }
    }

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        if (!function.validate()) {
            logger.i { "visitFunctionDeclaration(): $function is not valid" }
            return
        }
        if (!options.isValid(function, logger)) {
            return
        }
        diagrams.addFunction(function)
    }

    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
        if (!property.validate()) {
            logger.i { "visitPropertyDeclaration(): $property is not valid" }
            return
        }
        if (!options.isValid(property, logger)) {
            return
        }
        diagrams.addProperty(property)
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

    override fun visitTypeAlias(typeAlias: KSTypeAlias, data: Unit) {
        if (!typeAlias.validate()) {
            logger.i { "visitTypeAlias(): $typeAlias is not valid" }
            return
        }
        if (!options.isValid(typeAlias, logger)) {
            return
        }
        logger.v { "visitTypeAlias: ${typeAlias.fullQualifiedName} (alias of ${typeAlias.findActualType().fullQualifiedName})" }

        diagrams.addTypeAlias(typeAlias = typeAlias)

    }

}
