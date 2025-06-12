import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import jdk.javadoc.internal.doclets.toolkit.util.DocPath.parent
import uml.component.ClassEntry
import uml.component.DiagramComponent
import uml.component.EnumEntry
import uml.component.InterfaceEntry
import uml.component.ObjectEntry
import uml.fullQualifiedName
import uml.relation.GenericRelation
import uml.relation.InheritanceRelation
import uml.relation.PropertyRelation
import uml.relation.RelationshipComponent

class ClassDiagramDescription(val options: Options, val logger: KSPLogger? = null) {
    val componentBuilder = mutableListOf<DiagramComponent.Builder<*>>()
    val relationsBuilder = mutableListOf<RelationshipComponent.Builder<*>>()

    fun addClass(classDeclaration: KSClassDeclaration) {
        if (!options.isValid(classDeclaration, logger)) {
            logger.v { "Ignore ${classDeclaration.fullQualifiedName} since its not valid according the options" }
            return
        }
        if (options.showInheritance) {
            classDeclaration.superTypes
                .mapNotNull { it.resolve().declaration as? KSClassDeclaration }
                .filterNot { it.packageName.asString().startsWith("kotlin") }
                .forEach { parent ->
                    addHierarchy(classDeclaration, parent)
                }
        }
        when (classDeclaration.classKind) {
            ClassKind.CLASS -> {
                val builder = ClassEntry.Builder(clazz = classDeclaration, options = options, logger = logger)
                componentBuilder.add(builder)
                logger.v { "${classDeclaration.fullQualifiedName} added" }
                if (options.showRelations) {
                    addPropertyRelations(classDeclaration, builder)
                }
            }

            ClassKind.INTERFACE -> {
                val builder = InterfaceEntry.Builder(clazz = classDeclaration, options = options, logger = logger)
                componentBuilder.add(builder)
                logger.v { "${classDeclaration.fullQualifiedName} added" }
                if (options.showRelations) {
                    addPropertyRelations(classDeclaration, builder)
                }
            }

            ClassKind.ENUM_CLASS -> {
                val builder = EnumEntry.Builder(clazz = classDeclaration, options = options, logger = logger)
                componentBuilder.add(builder)
                logger.v { "${classDeclaration.fullQualifiedName} added" }
                if (options.showRelations) {
                    addPropertyRelations(classDeclaration, builder)
                }
            }

            ClassKind.OBJECT -> {
                val builder = ObjectEntry.Builder(clazz = classDeclaration, options = options, logger = logger)
                componentBuilder.add(builder)
                logger.v { "${classDeclaration.fullQualifiedName} added" }
                if (options.showRelations) {
                    addPropertyRelations(classDeclaration, builder)
                }
            }

            else -> Unit
        }

        val innerClasses = classDeclaration.declarations.mapNotNull { it as? KSClassDeclaration }.filter { !it.isCompanionObject }
        innerClasses.forEach {
            addClass(it)
        }
    }

    private fun addInvisibleLinkage(first: KSClassDeclaration, second: KSClassDeclaration) {
        relationsBuilder.add(GenericRelation.Builder(first, second, "-[hidden]-"))
    }

    private fun addHierarchy(child: KSClassDeclaration, parent: KSClassDeclaration) {
        when {

            !options.isValid(child, logger) ->
                logger.v { "Hierarchy between ${child.fullQualifiedName} and ${parent.fullQualifiedName} excluded due to invalid child" }

            !options.isValid(parent, logger) ->
                logger.v { "Hierarchy between ${child.fullQualifiedName} and ${parent.fullQualifiedName} excluded due to invalid parent" }

            else ->
                relationsBuilder.add(InheritanceRelation.Builder(child, parent))

        }
    }

    private fun addPropertyRelations(base: KSClassDeclaration, builder: DiagramComponent.Builder<*>) {
        when {
            !options.isValid(base, logger) ->
                logger.v { "Property relation between ${base.fullQualifiedName} and $builder excluded due to invalid property reference" }

            else -> {
                val attributes = (builder.build()?.attributes ?: emptyList())
                    .filterNot { it.attributeType.uniqueIdentifier.startsWith("kotlin") }
                    .filter { options.isValid(it.originalKSProperty) && options.isValid(it.attributeType.originalKSType) }
                attributes.forEach {
                    relationsBuilder.add(PropertyRelation.Builder(base, it))
                }
            }
        }
    }

    fun computeUMLClassDiagrams(options: Options? = null): String {
        return if (options?.showPackages == true) {
            componentBuilder.groupBy {
                (it as? InterfaceEntry.Builder)?.clazz?.packageName?.asString()
                    ?: (it as? ClassEntry.Builder)?.clazz?.packageName?.asString()
                    ?: (it as? EnumEntry.Builder)?.clazz?.packageName?.asString()
                    ?: (it as? ObjectEntry.Builder)?.clazz?.packageName?.asString()
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
}
