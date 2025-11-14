import com.google.devtools.ksp.findActualType
import com.google.devtools.ksp.processing.KSPLogger
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

        val classes = file.declarations.filterIsInstance<KSClassDeclaration>()
        classes.forEach {
            logger.v { "visitFile(): Visit $it in $file" }
            it.accept(this, data)
        }
        val functions = file.declarations.filterIsInstance<KSFunctionDeclaration>()
        functions.forEach {
            logger.v { "visitFile(): Visit $it in $file" }
            it.accept(this, data)
        }
        val properties = file.declarations.filterIsInstance<KSPropertyDeclaration>()
        properties.forEach {
            logger.v { "visitFile(): Visit $it in $file" }
            it.accept(this, data)
        }
        val typealiases = file.declarations.filterIsInstance<KSTypeAlias>()
        typealiases.forEach {
            logger.v { "visitFile(): Visit $it in $file" }
            it.accept(this, data)
        }
        val ignoredDeclarations = file.declarations.filterNot { it in listOf(typealiases, properties, functions, classes).flatMap { it.toList() } }.toList()
        if (ignoredDeclarations.isNotEmpty()) {
            logger.w { "The following declarations of file $file, were ignored: ${ignoredDeclarations.joinToString()}" }
        }
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
