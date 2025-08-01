import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.sun.org.apache.bcel.internal.Repository.addClass
import uml.DiagramElement
import uml.element.ClassElement
import uml.element.EnumElement
import uml.element.InterfaceElement
import uml.element.ObjectElement
import uml.fullQualifiedName
import uml.relation.ElementRelation

class ClassDiagramDescription(val options: Options, val logger: KSPLogger? = null) {
    val componentBuilder = mutableListOf<DiagramElement.Builder<*>>()
    val relationsBuilder = mutableListOf<ElementRelation.Builder>()
    val renderedComponents: List<KSClassDeclaration>
        get() = componentBuilder.map { it.clazz }

    private fun addHierarchy(child: KSClassDeclaration, parent: KSClassDeclaration) {
        when {

            !options.isValid(child, logger) ->
                logger.v { "Hierarchy between ${child.fullQualifiedName} and ${parent.fullQualifiedName} excluded due to invalid child" }

            !options.isValid(parent, logger) ->
                logger.v { "Hierarchy between ${child.fullQualifiedName} and ${parent.fullQualifiedName} excluded due to invalid parent" }

            child == parent ->
                logger.v { "Hierarchy between ${child.fullQualifiedName} and ${parent.fullQualifiedName} excluded due to reference to itself is ignored" }

            else ->
                when {
                    parent.fullQualifiedName !in renderedComponents.map { it.fullQualifiedName } ->
                        logger.v { "Hierarchy between ${child.fullQualifiedName} and ${parent.fullQualifiedName} excluded due Superclass is a not rendered class" }

                    child.fullQualifiedName !in renderedComponents.map { it.fullQualifiedName } ->
                        logger.v { "Hierarchy between ${child.fullQualifiedName} and ${parent.fullQualifiedName} excluded due derived class is a not rendered class" }

                    else ->
                        relationsBuilder.add(ElementRelation.InheritanceBuilder(child, parent))
                }
        }
    }

    private fun addPropertyRelations(builder: DiagramElement.Builder<DiagramElement>) = addPropertyRelations(base = builder.clazz, builder = builder)

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
                    .forEach { fieldOfClass ->
                        when {
                            fieldOfClass.attributeType.fullQualifiedName.startsWith("kotlin") ->
                                logger.v { "Property relation of field $fieldOfClass of class ${base.fullQualifiedName} excluded due kotlin std classes are ignored" }

                            fieldOfClass.attributeType.fullQualifiedName.startsWith("java") ->
                                logger.v { "Property relation of field $fieldOfClass of class ${base.fullQualifiedName} excluded due java std classes are ignored" }

                            !options.isValid(fieldOfClass.originalKSProperty, logger) || !options.isValid(fieldOfClass.attributeType.originalKSType, logger) ->
                                Unit // Reason is logged in the isValid invocation

                            base.fullQualifiedName == fieldOfClass.attributeType.fullQualifiedName ->
                                logger.v { "Property relation of field $fieldOfClass of class ${base.fullQualifiedName} excluded due to reference to itself, which are ignored" }

                            else ->
                                if (fieldOfClass.attributeType.fullQualifiedName !in renderedComponents.map { it.fullQualifiedName }) {
                                    logger.v { "Property relation of method $fieldOfClass of class ${base.fullQualifiedName} excluded due to return type of not rendered class" }
                                } else {
                                    relationsBuilder.add(ElementRelation.PropertyBuilder(base, fieldOfClass))
                                }
                        }
                    }
            }
        }
    }

    private fun addFunctionRelations(builder: DiagramElement.Builder<DiagramElement>) = addFunctionRelations(base = builder.clazz, builder = builder)

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
                    .forEach { methodOfClass ->
                        when {
                            methodOfClass.returnType.fullQualifiedName.startsWith("kotlin") ->
                                logger.v { "Function relation of method $methodOfClass of class ${base.fullQualifiedName} excluded due kotlin std classes are ignored" }

                            methodOfClass.returnType.fullQualifiedName.startsWith("java") ->
                                logger.v { "Function relation of method $methodOfClass of class ${base.fullQualifiedName} excluded due java std classes are ignored" }

                            !options.isValid(methodOfClass.originalKSFunctionDeclaration, logger) || !options.isValid(methodOfClass.returnType.originalKSType, logger) ->
                                Unit // Reason is logged in the isValid invocation

                            base.fullQualifiedName == methodOfClass.returnType.fullQualifiedName ->
                                logger.v { "Function relation of method $methodOfClass of class ${base.fullQualifiedName} excluded due to reference to itself, which are ignored" }

                            else -> {
                                if (methodOfClass.returnType.fullQualifiedName !in renderedComponents.map { it.fullQualifiedName }) {
                                    logger.v { "Function relation of method $methodOfClass of class ${base.fullQualifiedName} excluded due to return type of not rendered class" }
                                } else {
                                    relationsBuilder.add(ElementRelation.FunctionBuilder(base, methodOfClass))
                                }
                            }
                        }
                    }
            }
        }
    }

    private fun computeUMLDiagramsWithPackages() = componentBuilder.groupBy {
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

    /**
     * Creates or updates [DiagramElement.Builder] for the given [KSClassDeclaration] and its inner classes, if the given [KSClassDeclaration] is valid.
     * See also: [Options.isValid]
     *
     * @param classDeclaration The [KSClassDeclaration] that should be converted to a plantuml format conform description
     * @param isShell Whether the created or updated [DiagramElement.Builder] for the given [classDeclaration] should render all fields and functions or just the exclusively added ones.
     */
    fun addClass(classDeclaration: KSClassDeclaration, isShell: Boolean = false) {
        if (!options.isValid(classDeclaration, logger)) {
            logger.v { "Ignore ${classDeclaration.fullQualifiedName} since its not valid according the options" }
            return
        }

        val existingBuilder = componentBuilder.find { it.clazz == classDeclaration }
        if (existingBuilder != null) {
            when {
                !existingBuilder.isShell -> {
                    logger.v { "${classDeclaration.fullQualifiedName} was added previously" }
                }

                !isShell -> {
                    logger.v { "${classDeclaration.fullQualifiedName} was added previously as shell, now its marked as complete" }
                    existingBuilder.isShell = false
                    addInnerClasses(classDeclaration)
                }
            }
            return
        }

        when (classDeclaration.classKind) {
            ClassKind.CLASS -> {
                val builder = ClassElement.Builder(clazz = classDeclaration, options = options, logger = logger, isShell = isShell)
                componentBuilder.add(builder)
                logger.v { "${classDeclaration.fullQualifiedName} added" }
            }

            ClassKind.INTERFACE -> {
                val builder = InterfaceElement.Builder(clazz = classDeclaration, options = options, logger = logger, isShell = isShell)
                componentBuilder.add(builder)
                logger.v { "${classDeclaration.fullQualifiedName} added" }
            }

            ClassKind.ENUM_CLASS -> {
                val builder = EnumElement.Builder(clazz = classDeclaration, options = options, logger = logger, isShell = isShell)
                componentBuilder.add(builder)
                logger.v { "${classDeclaration.fullQualifiedName} added" }
            }

            ClassKind.OBJECT -> {
                val builder = ObjectElement.Builder(clazz = classDeclaration, options = options, logger = logger, isShell = isShell)
                componentBuilder.add(builder)
                logger.v { "${classDeclaration.fullQualifiedName} added" }
            }

            else -> Unit
        }

        if (!isShell) {
            addInnerClasses(classDeclaration)
        }
    }

    private fun addInnerClasses(classDeclaration: KSClassDeclaration) {
        val innerClasses = classDeclaration.declarations.mapNotNull { it as? KSClassDeclaration }.filter { !it.isCompanionObject }
        innerClasses.forEach {
            addClass(it, false)
        }
    }

    /**
     * Creates or updates [DiagramElement.Builder] for the owner of the given [KSFunctionDeclaration], if the given [KSFunctionDeclaration] is valid.
     * See also: [Options.isValid]
     *
     * @param function The [KSFunctionDeclaration] that should be visualized
     */
    fun addFunction(function: KSFunctionDeclaration) {
        val classOfExtensionFunction = function.extensionReceiver?.resolve()?.declaration as? KSClassDeclaration
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
                    logger.v { "Add extension function $function to builder of $functionOwningClass" }
                    builder.extensionFunctions.add(function)
                } else {
                    logger.w { "No builder found for class $functionOwningClass -> add class as shell first then add extension function $function" }
                    addClass(functionOwningClass, true)
                    componentBuilder.find { it.clazz == functionOwningClass }?.extensionFunctions?.add(function)
                }
            }
        }
    }

    /**
     * Creates or updates [DiagramElement.Builder] for the owner of the given [KSPropertyDeclaration], if the given [KSPropertyDeclaration] is valid.
     * See also: [Options.isValid]
     *
     * @param property The [KSPropertyDeclaration] that should be visualized
     */
    fun addProperty(property: KSPropertyDeclaration) {
        val classOfExtensionVariable = property.extensionReceiver?.resolve()?.declaration as? KSClassDeclaration
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
                    logger.v { "Add extension property $property to builder of $variableOwningClass" }
                    builder.extensionProperties.add(property)
                } else {
                    logger.w { "No builder found for class $variableOwningClass -> add Class as shell first then add extension property $property" }
                    addClass(variableOwningClass, true)
                    componentBuilder.find { it.clazz == variableOwningClass }?.extensionProperties?.add(property)
                }
            }
        }
    }

    /**
     * Renders all classes that were added through [addClass] to a plantuml conform format.
     *
     * @return plantuml conform description of all added classes
     */
    fun computeUMLClassDiagrams(): String {
        return if (options.showPackages) {
            computeUMLDiagramsWithPackages()
        } else {
            componentBuilder.mapNotNull { it.build() }.joinToString("\n") { it.render() }
        }
    }

    /**
     * Renders all inheritances of all classes that were added through [addClass] to a plantuml conform format.
     *
     * @return plantuml conform arrows from derived classes to the super classes
     */
    fun computeInheritanceRelations(): String {
        componentBuilder.forEach { builder ->
            builder.clazz.superTypes
                .mapNotNull { it.resolve().declaration as? KSClassDeclaration }
                .filterNot { it.packageName.asString().startsWith("kotlin") }
                .filterNot { it.packageName.asString().startsWith("java") }
                .forEach { parent ->
                    addHierarchy(builder.clazz, parent)
                }
        }
        return relationsBuilder.filterIsInstance<ElementRelation.InheritanceBuilder>().joinToString("\n") { it.build().render() }.split("\n").distinct().joinToString("\n")
    }

    /**
     * Renders all types of properties for all classes that were added through [addClass] to a plantuml conform format.
     *
     * @return plantuml conform arrows from properties of classes to the type classes
     */
    fun computePropertyRelations(): String {
        componentBuilder
            .filterIsInstance<DiagramElement.Builder<DiagramElement>>()
            .forEach { builder ->
                addPropertyRelations(builder)
            }
        return relationsBuilder.filterIsInstance<ElementRelation.PropertyBuilder>().joinToString("\n") { it.build().render() }.split("\n").distinct().joinToString("\n")
    }

    /**
     * Renders all return types of functions for all classes that were added through [addClass] to a plantuml conform format.
     *
     * @return plantuml conform arrows from functions of classes to the return type classes
     */
    fun computeFunctionRelations(): String {
        componentBuilder
            .filterIsInstance<DiagramElement.Builder<DiagramElement>>()
            .forEach { builder ->
                addFunctionRelations(builder)
            }
        return relationsBuilder.filterIsInstance<ElementRelation.FunctionBuilder>().joinToString("\n") { it.build().render() }.split("\n").distinct().joinToString("\n")
    }

}
