package uml.relation

import Options
import com.google.devtools.ksp.symbol.KSClassDeclaration
import uml.component.DiagramComponent.Companion.INDENT

data class InheritanceRelation(val childAlias: String, val parentAlias: String) : RelationshipComponent {
    class Builder(val child: KSClassDeclaration, val parent: KSClassDeclaration, override val options: Options? = null) : RelationshipComponent.Builder<InheritanceRelation> {
        override fun build(): InheritanceRelation {
            return InheritanceRelation(
                childAlias = "${(child.qualifiedName?.getQualifier() ?: child.packageName.asString()).replace(".", "_")}_${child.simpleName.asString()}",
                parentAlias = "${(parent.qualifiedName?.getQualifier() ?: parent.packageName.asString()).replace(".", "_")}_${parent.simpleName.asString()}"
            )
        }
    }

    override fun render(): String {
        return "$INDENT$parentAlias <|-- $childAlias"
    }
}
