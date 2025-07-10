package uml.relation

import Options
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import uml.Renderable
import uml.element.Field
import uml.element.Method
import uml.element.toType
import uml.fullQualifiedName

data class ElementRelation(val headAlias: String, val tailAlias: String, val relationKind: RelationKind) : Renderable {
    override fun render(): String {
        return """$headAlias ${relationKind.arrow} $tailAlias"""
    }

    interface Builder {
        val options: Options?
        fun build(): ElementRelation?
    }

    class AggregationBuilder(val owner: KSClassDeclaration, val type: KSType, override val options: Options? = null) : Builder {
        override fun build(): ElementRelation {
            return ElementRelation(
                headAlias = "${(owner.qualifiedName?.getQualifier() ?: owner.packageName.asString()).replace(".", "_")}_${owner.simpleName.asString()}",
                tailAlias = type.toType().typeName,
                RelationKind.Aggregation
            )
        }
    }

    class InheritanceBuilder(val child: KSClassDeclaration, val parent: KSClassDeclaration, override val options: Options? = null) : Builder {
        override fun build(): ElementRelation {
            return ElementRelation(
                tailAlias = child.fullQualifiedName.replace(".", "_").trim('_'),
                headAlias = parent.fullQualifiedName.replace(".", "_").trim('_'),
                relationKind = RelationKind.Inheritance
            )
        }
    }

    class PropertyBuilder(val classDeclaration: KSClassDeclaration, val classAttribute: Field, override val options: Options? = null, val logger: KSPLogger? = null) : Builder {
        override fun build(): ElementRelation {
            val classNameAlias = classDeclaration.fullQualifiedName.replace(".", "_").trim('_')
            val propertyName = classAttribute.attributeName
            val typeAlias = classAttribute.attributeType.fullQualifiedName.replace(".", "_").trim('_')
            return ElementRelation("$classNameAlias::$propertyName", typeAlias, RelationKind.Property)
        }
    }

    class FunctionBuilder(val classDeclaration: KSClassDeclaration, val classMethod: Method, override val options: Options? = null, val logger: KSPLogger? = null) : Builder {
        override fun build(): ElementRelation {
            val classNameAlias = classDeclaration.fullQualifiedName.replace(".", "_").trim('_')
            val propertyName = classMethod.functionName
            val typeAlias = classMethod.returnType.fullQualifiedName.replace(".", "_").trim('_')
            return ElementRelation("$classNameAlias::$propertyName", typeAlias, RelationKind.Property)
        }
    }
}
