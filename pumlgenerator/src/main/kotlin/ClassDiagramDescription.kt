import OptionConstants.KEY_SHOW_INDIRECT_RELATIONS
import com.google.devtools.ksp.findActualType
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.sun.org.apache.bcel.internal.Repository.addClass
import graph.FunctionRelation
import graph.IndirectFunctionRelation
import graph.IndirectPropertyRelation
import graph.InheritanceRelation
import graph.PropertyRelation
import graph.Relation
import graph.RelationGraph
import graph.RelationKind
import uml.DiagramElement
import uml.element.ClassElement
import uml.element.EnumElement
import uml.element.InterfaceElement
import uml.element.ObjectElement
import uml.element.ReservedType
import uml.element.TypealiasElement
import uml.element.flatResolve
import uml.fullQualifiedName

class ClassDiagramDescription(val options: Options, val logger: KSPLogger? = null) {
    val componentBuilder = mutableListOf<DiagramElement.Builder<*>>()

    val renderedComponents: List<KSClassDeclaration>
        get() = componentBuilder.map { it.clazz }

    val relationGraph = RelationGraph()

    private fun addHierarchy(child: KSClassDeclaration, parent: KSClassDeclaration) {
        when {
            !options.showInheritance ->
                logger.v { "Hierarchy between ${child.fullQualifiedName} and ${parent.fullQualifiedName} excluded due to option ${OptionConstants.KEY_SHOW_INHERITANCE}=false" }

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

                    else -> {
                        relationGraph.addRelation(InheritanceRelation(child, parent))
                    }
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
                    is TypealiasElement.Builder -> builder.build()?.attributes
                    else -> emptyList()
                } ?: emptyList()
                attributes
                    .forEach { fieldOfClass ->
                        when {
                            !options.showPropertyRelations ->
                                logger.v { "Property relation of field $fieldOfClass of class ${base.fullQualifiedName} excluded due to option ${OptionConstants.KEY_SHOW_PROPERTY_RELATIONS}=false" }

                            fieldOfClass.isPrimitive ->
                                logger.v { "Property relation of field $fieldOfClass of class ${base.fullQualifiedName} excluded due kotlin primitive classes are ignored" }

                            fieldOfClass.attributeType.fullQualifiedName.startsWith("java") ->
                                logger.v { "Property relation of field $fieldOfClass of class ${base.fullQualifiedName} excluded due java std classes are ignored (${fieldOfClass.attributeType.fullQualifiedName})" }

                            !options.isValid(fieldOfClass.originalKSProperty, logger) || !options.isValid(fieldOfClass.attributeType.originalKSType, logger) ->
                                Unit // Reason is logged in the isValid invocation

                            base.fullQualifiedName == fieldOfClass.attributeType.fullQualifiedName ->
                                logger.v { "Property relation of field $fieldOfClass of class ${base.fullQualifiedName} excluded due to reference to itself, which are ignored" }

                            else -> {
                                if (!fieldOfClass.attributeType.isGeneric && !fieldOfClass.attributeType.isCollection && fieldOfClass.attributeType !is ReservedType) {
                                    logger.i { "Add Relation ${PropertyRelation(base, fieldOfClass)}" }
                                    relationGraph.addRelation(PropertyRelation(base, fieldOfClass))
                                } else {
                                    val types = fieldOfClass.attributeType.flatResolve(options = options, logger = logger)

                                    if (types.isNotEmpty()) {
                                        types.forEach { (type, level) ->
                                            when {
                                                level == 0 ->
                                                    PropertyRelation(classDeclaration = base, classAttribute = fieldOfClass, fieldType = type)

                                                level > 0 && options.showIndirectRelations ->
                                                    IndirectPropertyRelation(classDeclaration = base, classAttribute = fieldOfClass, fieldType = type)

                                                else -> {
                                                    logger.v { "Ignore Relation of $fieldOfClass due to $KEY_SHOW_INDIRECT_RELATIONS=false" }
                                                    null
                                                }
                                            }?.let { relation ->
                                                if (base.fullQualifiedName == type.fullQualifiedName) {
                                                    logger.v { "Ignore Relation to itself: $relation" }
                                                } else {
                                                    logger.i { "Add Relation $relation" }
                                                    relationGraph.addRelation(relation)
                                                }
                                            }
                                        }
                                    } else {
                                        logger.w { "$fieldOfClass resolved no types" }
                                    }
                                }
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
                    is TypealiasElement.Builder -> builder.build()?.functions
                    else -> emptyList()
                } ?: emptyList()
                functions
                    .forEach { methodOfClass ->
                        when {
                            !options.showFunctionRelations ->
                                logger.v { "Function relation of method $methodOfClass of class ${base.fullQualifiedName} excluded due to option ${OptionConstants.KEY_SHOW_PROPERTY_RELATIONS}=false" }

                            methodOfClass.returnType.isPrimitive ->
                                logger.v { "Function relation of method $methodOfClass of class ${base.fullQualifiedName} excluded due kotlin primitives are ignored" }

                            methodOfClass.returnType.fullQualifiedName.startsWith("java") ->
                                logger.v { "Function relation of method $methodOfClass of class ${base.fullQualifiedName} excluded due java std classes are ignored" }

                            !options.isValid(methodOfClass.originalKSFunctionDeclaration, logger) || !options.isValid(methodOfClass.returnType.originalKSType, logger) ->
                                Unit // Reason is logged in the isValid invocation

                            base.fullQualifiedName == methodOfClass.returnType.fullQualifiedName ->
                                logger.v { "Function relation of method $methodOfClass of class ${base.fullQualifiedName} excluded due to reference to itself, which are ignored" }

                            else -> {
                                if (!methodOfClass.returnType.isGeneric && !methodOfClass.returnType.isCollection && methodOfClass.returnType !is ReservedType) {
                                    logger.i { "Add Relation ${FunctionRelation(base, methodOfClass)}" }
                                    relationGraph.addRelation(FunctionRelation(base, methodOfClass))
                                } else {
                                    val types = methodOfClass.returnType.flatResolve(options = options, logger = logger)

                                    if (types.isNotEmpty()) {
                                        types.forEach { (type, level) ->
                                            when {
                                                level == 0 ->
                                                    FunctionRelation(classDeclaration = base, classMethod = methodOfClass, returnType = type)

                                                level > 0 && options.showIndirectRelations ->
                                                    IndirectFunctionRelation(classDeclaration = base, classMethod = methodOfClass, returnType = type)

                                                else -> {
                                                    logger.v { "Ignore Relation of $methodOfClass due to $KEY_SHOW_INDIRECT_RELATIONS=false" }
                                                    null
                                                }
                                            }?.let { relation ->
                                                if (base.fullQualifiedName == type.fullQualifiedName) {
                                                    logger.v { "Ignore Relation to itself: $relation" }
                                                } else {
                                                    logger.i { "Add Relation $relation" }
                                                    relationGraph.addRelation(relation)
                                                }
                                            }
                                        }
                                    } else {
                                        logger.w { "$methodOfClass resolved no return types" }
                                    }
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


    fun computeAllRelations(): String {
        componentBuilder.forEach { builder ->
            builder.clazz.superTypes
                .mapNotNull { it.resolve().declaration as? KSClassDeclaration }
                .filterNot { it.packageName.asString().startsWith("kotlin") }
                .filterNot { it.packageName.asString().startsWith("java") }
                .forEach { parent ->
                    addHierarchy(builder.clazz, parent)
                }
        }
        componentBuilder
            .filterIsInstance<DiagramElement.Builder<DiagramElement>>()
            .forEach { builder ->
                addFunctionRelations(builder)
                addPropertyRelations(builder)
            }
        val blacklistedVertices = mutableListOf<String>()
        val allEdges = mutableMapOf<Pair<String, String>, Relation>()
        blacklistedVertices.addAll(relationGraph.vertices.filter { relationGraph.inDegreeOf(it) > options.maxRelations || relationGraph.outDegreeOf(it) > options.maxRelations })
        return computationVariant2(allEdges, blacklistedVertices)
    }

    /*
    Rules:
    Relations are sorted by their relationKind (Inheritance first, Indirect relation last)
    Relations are also sorted by their outgoing edges (many dependencies first, the least dependencies last)
    Inheritance always from bottom to top (child below parent)
    Every dependency relation 1 layer below
     */
    private fun computationVariant1(allEdges: MutableMap<Pair<String, String>, Relation>, blacklistedVertices: MutableList<String>): String {
        return buildString {
            blacklistedVertices.forEach {
                appendLine(
                    """
note top of $it
Relations of $it cannot be shown
based on to many relations
end note
""".trimIndent()
                )
            }
            // Start with vertices with most edges & End with vertices with the least edges
            relationGraph.vertices.sortedBy { relationGraph.inDegreeOf(it) + relationGraph.outDegreeOf(it) }.reversed().forEach {
                // Start with edges to vertices with most out edges & End with vertices with least out edges
                val outRelations = relationGraph.outEdgesOf(it).sortedWith(
                    compareBy(
                        { edge -> edge.relationKind },
                        { edge -> relationGraph.outDegreeOf(edge.toAlias) * (-1) })
                )
                outRelations.forEachIndexed { index, relation ->
                    // Avoid adding edge multiple times
                    when {
                        allEdges.containsKey(relation.fromAlias to relation.toAlias) ->
                            logger.v { "Edge for $relation exists already" }

                        relation.relationKind == RelationKind.Inheritance ->
                            appendLine("${relation.toAlias} ${relation.relationKind.reversedArrow} ${relation.fromAlias}")

                        relation.fromAlias in blacklistedVertices ->
                            logger.v { "Edges for ${relation.fromAlias} are blacklisted by the amount of allowed edges" }

                        relation.toAlias in blacklistedVertices ->
                            logger.v { "Edges for ${relation.toAlias} are blacklisted by the amount of allowed edges" }

                        else -> {
                            allEdges[relation.fromAlias to relation.toAlias] = relation
                            appendLine("${relation.fromAlias} ${relation.relationKind.arrowWithLevel(1)} ${relation.toAlias}")
                        }
                    }
                }
            }
        }
    }

    /*
    Rules:
    Relations are sorted by their relationKind (Inheritance first, Indirect relation last)
    Relations are also sorted by their outgoing edges (many dependencies first, the least dependencies last)
    Inheritance always from bottom to top (child below parent)
    If a Relation exists from A to B and B to A, the same layer will be used
    Otherwise A is below B
     */
    private fun computationVariant2(allEdges: MutableMap<Pair<String, String>, Relation>, blacklistedVertices: MutableList<String>): String {
        return buildString {
            blacklistedVertices.forEach {
                appendLine(
                    """
note top of $it
Relations of $it cannot be shown
based on to many relations
end note
""".trimIndent()
                )
            }
            // Start with vertices with most edges & End with vertices with the least edges
            relationGraph.vertices.sortedBy { relationGraph.inDegreeOf(it) + relationGraph.outDegreeOf(it) }.forEach {
                relationGraph.outEdgesOf(it).sortedWith(
                    compareBy(
                        { edge -> edge.relationKind },
                        { edge -> relationGraph.outDegreeOf(edge.toAlias) * (-1) })
                ).forEachIndexed { index, relation ->
                    when {
                        allEdges.containsKey(relation.fromAlias to relation.toAlias) ->
                            logger.v { "Edge for $relation exists already" }

                        relation.relationKind == RelationKind.Inheritance ->
                            appendLine("${relation.toAlias} ${relation.relationKind.reversedArrow} ${relation.fromAlias}")

                        relation.fromAlias in blacklistedVertices ->
                            logger.v { "Edges for ${relation.fromAlias} are blacklisted by the amount of allowed edges" }

                        relation.toAlias in blacklistedVertices ->
                            logger.v { "Edges for ${relation.toAlias} are blacklisted by the amount of allowed edges" }

                        else -> {
                            allEdges[relation.fromAlias to relation.toAlias] = relation
                            if (relationGraph.hasEdge(relation.toAlias, relation.fromAlias) && relationGraph.hasEdge(relation.fromAlias, relation.toAlias)) {
                                appendLine("${relation.fromAlias} ${relation.relationKind.arrowWithLevel(0)} ${relation.toAlias}")
                            } else {
                                appendLine("${relation.fromAlias} ${relation.relationKind.arrowWithLevel(1)} ${relation.toAlias}")
                            }
                        }
                    }
                }
            }
        }
    }

    fun addTypeAlias(typeAlias: KSTypeAlias) {
        val builder = TypealiasElement.Builder(typeAlias = typeAlias, clazz = typeAlias.findActualType(), isShell = false, options = options, logger = logger)
        componentBuilder.add(builder)
    }
}
