package uml.relation

import Options
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import uml.component.ClassAttribute
import uml.component.DiagramComponent.Companion.INDENT
import uml.fullQualifiedName

data class PropertyRelation(val classNameAlias: String, val propertyName: String, val typeAlias: String) : RelationshipComponent {
    class Builder(val classDeclaration: KSClassDeclaration, val classAttribute: ClassAttribute, override val options: Options? = null, val logger: KSPLogger? = null) : RelationshipComponent.Builder<PropertyRelation> {

        override fun build(): PropertyRelation {
            return PropertyRelation(classNameAlias = classDeclaration.fullQualifiedName.replace(".","_").trim('_'), propertyName = classAttribute.attributeName, typeAlias = classAttribute.attributeType.fullQualifiedName.replace(".","_").trim('_'))
        }
    }

    override fun render(): String {
        return "$INDENT$classNameAlias::$propertyName --* $typeAlias"
    }
}
