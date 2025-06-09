import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import uml.component.ClassEntry
import uml.component.DiagramComponent
import uml.component.EnumEntry
import uml.component.InterfaceEntry
import uml.component.ObjectEntry
import uml.relation.GenericRelation
import uml.relation.InheritanceRelation
import uml.relation.PropertyRelations
import uml.relation.RelationshipComponent

class ClassDiagramDescription(val options: Options) {
    val componentBuilder = mutableListOf<DiagramComponent.Builder<*>>()
    val relationsBuilder = mutableListOf<RelationshipComponent.Builder<*>>()

    fun addClass(classDeclaration: KSClassDeclaration) {
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
                val builder = ClassEntry.Builder(clazz = classDeclaration, options = options)
                componentBuilder.add(builder)
                if (options.showRelations) {
                    addPropertyRelations(classDeclaration, builder)
                }
            }

            ClassKind.INTERFACE -> {
                val builder = InterfaceEntry.Builder(clazz = classDeclaration, options = options)
                componentBuilder.add(builder)
                if (options.showRelations) {
                    addPropertyRelations(classDeclaration, builder)
                }
            }

            ClassKind.ENUM_CLASS -> {
                val builder = EnumEntry.Builder(clazz = classDeclaration, options = options)
                componentBuilder.add(builder)
                if (options.showRelations) {
                    addPropertyRelations(classDeclaration, builder)
                }
            }

            ClassKind.OBJECT -> {
                val builder = ObjectEntry.Builder(clazz = classDeclaration, options = options)
                componentBuilder.add(builder)
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

    private fun addHierarchy(base: KSClassDeclaration, parent: KSClassDeclaration) {
        relationsBuilder.add(InheritanceRelation.Builder(base, parent))
    }

    private fun addPropertyRelations(base: KSClassDeclaration, builder: DiagramComponent.Builder<*>) {
        relationsBuilder.add(PropertyRelations.Builder(base, builder))
    }

    fun computeUMLClassDiagrams(options: Options? = null): String {
        return if (options?.showPackages == true) {
            componentBuilder.groupBy {
                (it as? InterfaceEntry.Builder)?.clazz?.packageName?.asString()
                    ?: (it as? ClassEntry.Builder)?.clazz?.packageName?.asString()
                    ?: (it as? EnumEntry.Builder)?.clazz?.packageName?.asString()
                    ?: (it as? ObjectEntry.Builder)?.clazz?.packageName?.asString()
            }.map { (packageName, builder) ->
                val classes = builder.mapNotNull { it.build() }.joinToString("\n") { it.render() }
                """
package ${packageName} {
    $classes
}
            """
            }.joinToString("\n")
        } else {
            componentBuilder.mapNotNull { it.build() }.joinToString("\n") { it.render() }
        }
    }

    fun computeHierarchies(): String {
        return relationsBuilder.mapNotNull { it.build()?.render() }.joinToString("\n").split("\n").distinct().joinToString("\n")
    }
}
