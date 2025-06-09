package uml.relation

import Options
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import uml.component.toType

class AggregationRelation(val ownerAlias: String, val ownedAlias: String) : RelationshipComponent {

    class Builder(val owner: KSClassDeclaration, val type:KSType, override val options: Options? = null) : RelationshipComponent.Builder<AggregationRelation> {
        override fun build(): AggregationRelation {
            return AggregationRelation(
                ownerAlias = "${(owner.qualifiedName?.getQualifier() ?: owner.packageName.asString()).replace(".", "_")}_${owner.simpleName.asString()}",
                ownedAlias = type.toType().typeAlias
            )
        }
    }

    override fun render(): String {
        return """$ownerAlias o-- $ownedAlias"""
    }
}