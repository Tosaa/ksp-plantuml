package uml.relation

import Options
import com.google.devtools.ksp.symbol.KSClassDeclaration
import uml.component.DiagramComponent
import uml.fullQualifiedName


data class PropertyRelations(val relations: List<String>) : RelationshipComponent {
    class Builder(val classDeclaration: KSClassDeclaration, val classBuilder: DiagramComponent.Builder<*>, override val options: Options? = null) : RelationshipComponent.Builder<PropertyRelations> {

        private fun renderRelation(className: String, propertyName: String, type: String): String {
            return "$className::$propertyName --* $type"
        }

        override fun build(): PropertyRelations {
            val qualifiedClassName = classDeclaration.fullQualifiedName.replace(".", "_")
            val attributes = (classBuilder.build()?.attributes ?: emptyList()).filterNot { it.attributeType.uniqueIdentifier.startsWith("kotlin") }
            return PropertyRelations(
                attributes.map {
                    renderRelation(className = qualifiedClassName, propertyName = it.attributeName, type = it.attributeType.fullQualifiedName)
                }.toList()
            )
        }
    }

    override fun render(): String {
        return relations.joinToString("\n") { "\t\t$it" }
    }
}
