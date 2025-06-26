import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import jdk.javadoc.internal.doclets.toolkit.util.DocPath.parent
import uml.element.ClassElement
import uml.DiagramElement
import uml.element.EnumElement
import uml.element.InterfaceElement
import uml.element.ObjectElement
import uml.fullQualifiedName
import uml.relation.ElementRelation

class ClassDiagramDescription(val options: Options, val logger: KSPLogger? = null) {
    val componentBuilder = mutableListOf<DiagramElement.Builder<*>>()
    val relationsBuilder = mutableListOf<ElementRelation.Builder>()

    fun addClass(classDeclaration: KSClassDeclaration) {
        if (!options.isValid(classDeclaration, logger)) {
            logger.v { "Ignore ${classDeclaration.fullQualifiedName} since its not valid according the options" }
            return
        }

        val existingBuilder = componentBuilder.find { it.clazz == classDeclaration }
        if (existingBuilder != null) {
            logger.v { "${classDeclaration.fullQualifiedName} was added previously" }
            return
        }

        if (options.showInheritance) {
            classDeclaration.superTypes
                .mapNotNull { it.resolve().declaration as? KSClassDeclaration }
                .filterNot { it.packageName.asString().startsWith("kotlin") }
                .filterNot { it.packageName.asString().startsWith("java") }
                .forEach { parent ->
                    addHierarchy(classDeclaration, parent)
                }
        }
        when (classDeclaration.classKind) {
            ClassKind.CLASS -> {
                val builder = ClassElement.Builder(clazz = classDeclaration, options = options, logger = logger)
                componentBuilder.add(builder)
                logger.v { "${classDeclaration.fullQualifiedName} added" }
                if (options.showPropertyRelations) {
                    addPropertyRelations(classDeclaration, builder)
                }
                if (options.showFunctionRelations) {
                    addFunctionRelations(classDeclaration, builder)
                }
            }

            ClassKind.INTERFACE -> {
                val builder = InterfaceElement.Builder(clazz = classDeclaration, options = options, logger = logger)
                componentBuilder.add(builder)
                logger.v { "${classDeclaration.fullQualifiedName} added" }
                if (options.showPropertyRelations) {
                    addPropertyRelations(classDeclaration, builder)
                }
                if (options.showFunctionRelations) {
                    addFunctionRelations(classDeclaration, builder)
                }
            }

            ClassKind.ENUM_CLASS -> {
                val builder = EnumElement.Builder(clazz = classDeclaration, options = options, logger = logger)
                componentBuilder.add(builder)
                logger.v { "${classDeclaration.fullQualifiedName} added" }
                if (options.showPropertyRelations) {
                    addPropertyRelations(classDeclaration, builder)
                }
                if (options.showFunctionRelations) {
                    addFunctionRelations(classDeclaration, builder)
                }
            }

            ClassKind.OBJECT -> {
                val builder = ObjectElement.Builder(clazz = classDeclaration, options = options, logger = logger)
                componentBuilder.add(builder)
                logger.v { "${classDeclaration.fullQualifiedName} added" }
                if (options.showPropertyRelations) {
                    addPropertyRelations(classDeclaration, builder)
                }
                if (options.showFunctionRelations) {
                    addFunctionRelations(classDeclaration, builder)
                }
            }

            else -> Unit
        }

        val innerClasses = classDeclaration.declarations.mapNotNull { it as? KSClassDeclaration }.filter { !it.isCompanionObject }
        innerClasses.forEach {
            addClass(it)
        }
    }

    private fun addHierarchy(child: KSClassDeclaration, parent: KSClassDeclaration) {
        when {

            !options.isValid(child, logger) ->
                logger.v { "Hierarchy between ${child.fullQualifiedName} and ${parent.fullQualifiedName} excluded due to invalid child" }

            !options.isValid(parent, logger) ->
                logger.v { "Hierarchy between ${child.fullQualifiedName} and ${parent.fullQualifiedName} excluded due to invalid parent" }

            child == parent ->
                logger.v { "Hierarchy between ${child.fullQualifiedName} and ${parent.fullQualifiedName} excluded due to reference to itself is ignored" }

            else ->
                relationsBuilder.add(ElementRelation.InheritanceBuilder(child, parent))
        }
    }

    private fun addPropertyRelations(base: KSClassDeclaration, builder: DiagramElement.Builder<*>) {
        when {
            !options.isValid(base, logger) ->
                logger.v { "Property relations of ${base.fullQualifiedName} are excluded due to invalid KSClassDeclaration" }

            else -> {
                val attributes = when (builder) {
                    is ClassElement.Builder -> builder.build()?.attributes
                    is InterfaceElement.Builder -> builder.build()?.attributes
                    is ObjectElement.Builder -> builder.build()?.attributes
                    is EnumElement.Builder -> builder.build()?.attributes
                    else -> emptyList()
                } ?: emptyList()
                attributes
                    .filterNot { it.attributeType.fullQualifiedName.startsWith("kotlin") }
                    .filterNot { it.attributeType.fullQualifiedName.startsWith("java") }
                    .filter { options.isValid(it.originalKSProperty) && options.isValid(it.attributeType.originalKSType) }
                    .forEach { fieldOfClass ->
                        if (base.fullQualifiedName == fieldOfClass.attributeType.fullQualifiedName) {
                            logger.v { "Property relation of field $fieldOfClass of class ${base.fullQualifiedName} excluded due to reference to itself, which are ignored" }
                        } else {
                            relationsBuilder.add(ElementRelation.PropertyBuilder(base, fieldOfClass))
                        }
                    }
            }
        }
    }

    private fun addFunctionRelations(base: KSClassDeclaration, builder: DiagramElement.Builder<*>) {
        when {
            !options.isValid(base, logger) ->
                logger.v { "Function relations of ${base.fullQualifiedName} are excluded due to invalid KSClassDeclaration" }


            else -> {
                val functions = when (builder) {
                    is ClassElement.Builder -> builder.build()?.functions
                    is InterfaceElement.Builder -> builder.build()?.functions
                    is ObjectElement.Builder -> builder.build()?.functions
                    is EnumElement.Builder -> builder.build()?.functions
                    else -> emptyList()
                } ?: emptyList()
                functions
                    .filterNot { it.returnType.fullQualifiedName.startsWith("kotlin") }
                    .filterNot { it.returnType.fullQualifiedName.startsWith("java") }
                    .filter { options.isValid(it.originalKSFunctionDeclaration) && options.isValid(it.returnType.originalKSType) }
                    .forEach { methodOfClass ->
                        if (base.fullQualifiedName == methodOfClass.returnType.fullQualifiedName) {
                            logger.v { "Function relation of method $methodOfClass of class ${base.fullQualifiedName} excluded due to reference to itself, which are ignored" }
                        } else {
                            relationsBuilder.add(ElementRelation.FunctionBuilder(base, methodOfClass))
                        }
                    }
            }
        }
    }

    fun computeUMLClassDiagrams(options: Options? = null): String {
        return if (options?.showPackages == true) {
            componentBuilder.groupBy {
                (it as? InterfaceElement.Builder)?.clazz?.packageName?.asString()
                    ?: (it as? ClassElement.Builder)?.clazz?.packageName?.asString()
                    ?: (it as? EnumElement.Builder)?.clazz?.packageName?.asString()
                    ?: (it as? ObjectElement.Builder)?.clazz?.packageName?.asString()
            }.mapNotNull { (packageName, builder) ->
                val classComponents = builder.mapNotNull { it.build() }
                if (classComponents.isEmpty()) {
                    logger.v { "Ignore package $packageName since it contains no classes" }
                    return@mapNotNull null
                }
                val usedPackageName: String = when {
                    packageName == null && options.allowEmptyPackage -> ""
                    packageName == null && !options.allowEmptyPackage -> {
                        logger.v { "Ignore package $packageName since empty packageName is not allowed" }
                        return@mapNotNull null
                    }

                    packageName != null && packageName.isBlank() && options.allowEmptyPackage -> ""
                    packageName != null && packageName.isBlank() && !options.allowEmptyPackage -> {
                        logger.v { "Ignore package $packageName since empty packageName is not allowed" }
                        return@mapNotNull null
                    }

                    !options.isValid(packageName ?: "", logger) -> {
                        logger.v { "Ignore package $packageName since it is excluded" }
                        return@mapNotNull null
                    }

                    else ->
                        packageName ?: ""
                }

                val classes = classComponents.joinToString("\n") { it.render() }
                if (usedPackageName.isNotBlank()) {
                    """
package $usedPackageName {
    $classes
}
"""
                } else {
                    classes
                }

            }.filter { it.isNotBlank() }.joinToString("\n")
        } else {
            componentBuilder.mapNotNull { it.build() }.joinToString("\n") { it.render() }
        }
    }

    fun computeHierarchies(): String {
        return relationsBuilder.mapNotNull { it.build()?.render() }.joinToString("\n").split("\n").distinct().joinToString("\n")
    }

    fun addFunction(function: KSFunctionDeclaration) {
        val classOfExtensionFunction = function.extensionReceiver?.resolve()?.declaration?.closestClassDeclaration()
        when {
            classOfExtensionFunction == null -> {
                logger.w { "addFunction(): Could not resolve Class for extension function $function" }
                return
            }

            (!options.isValid(classOfExtensionFunction, logger)) ->
                return

            else -> {
                val functionOwningClass = if (classOfExtensionFunction.isCompanionObject) {
                    classOfExtensionFunction.parentDeclaration as? KSClassDeclaration ?: classOfExtensionFunction
                } else {
                    classOfExtensionFunction
                }
                val builder = componentBuilder.find { it.clazz == functionOwningClass }
                if (builder != null) {
                    builder.functions.add(function)
                } else {
                    logger.w { "No builder found for class $functionOwningClass -> addClass first then add extension function" }
                    addClass(classOfExtensionFunction)
                    componentBuilder.find { it.clazz == functionOwningClass }?.functions?.add(function)
                }
            }
        }
    }

    fun addProperty(property: KSPropertyDeclaration) {
        val classOfExtensionVariable = property.extensionReceiver?.resolve()?.declaration?.closestClassDeclaration()
        when {
            classOfExtensionVariable == null -> {
                logger.w { "addFunction(): Could not resolve Class for extension variable $property" }
                return
            }

            (!options.isValid(classOfExtensionVariable, logger)) ->
                return

            else -> {
                val variableOwningClass = if (classOfExtensionVariable.isCompanionObject) {
                    classOfExtensionVariable.parentDeclaration as? KSClassDeclaration ?: classOfExtensionVariable
                } else {
                    classOfExtensionVariable
                }
                val builder = componentBuilder.find { it.clazz == classOfExtensionVariable }
                if (builder != null) {
                    builder.properties.add(property)
                } else {
                    logger.w { "No builder found for class $variableOwningClass -> addClass first then add extension property" }
                    addClass(variableOwningClass)
                    componentBuilder.find { it.clazz == variableOwningClass }?.properties?.add(property)
                }
            }
        }
    }
}
