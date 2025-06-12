package uml.relation

import Options
import com.google.devtools.ksp.symbol.KSClassDeclaration
import uml.component.DiagramComponent.Companion.INDENT
import uml.fullQualifiedName

data class InheritanceRelation(val childAlias: String, val parentAlias: String) : RelationshipComponent {
    class Builder(val child: KSClassDeclaration, val parent: KSClassDeclaration, override val options: Options? = null) : RelationshipComponent.Builder<InheritanceRelation> {
        override fun build(): InheritanceRelation {
            return InheritanceRelation(
                childAlias = child.fullQualifiedName.replace(".","_").trim('_'),
                parentAlias = parent.fullQualifiedName.replace(".","_").trim('_')
            )
        }
    }

    override fun render(): String {
        return "$INDENT$parentAlias <|-- $childAlias"
    }
}
