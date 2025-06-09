package uml.relation

import Options
import com.google.devtools.ksp.symbol.KSClassDeclaration
import uml.fullQualifiedName


class GenericRelation(val ownerAlias: String, val ownedAlias: String, val relationArrow: String) : RelationshipComponent {

    class Builder(val owner: KSClassDeclaration, val other: KSClassDeclaration, val relationArrow: String, override val options: Options? = null) : RelationshipComponent.Builder<GenericRelation> {
        override fun build(): GenericRelation {
            return GenericRelation(
                ownerAlias = owner.fullQualifiedName,
                ownedAlias = other.fullQualifiedName,
                relationArrow = relationArrow
            )
        }
    }

    override fun render(): String {
        return """$ownerAlias o-- $ownedAlias"""
    }
}