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

interface ElementRelation : Renderable {

    interface Builder {
        val options: Options?
        fun build(): ElementRelation?
    }

    class AggregationBuilder(val owner: KSClassDeclaration, val type: KSType, override val options: Options? = null) : Builder {
        override fun build(): ElementRelation {
            return SimpleElementRelation(
                headAlias = "${(owner.qualifiedName?.getQualifier() ?: owner.packageName.asString()).replace(".", "_")}_${owner.simpleName.asString()}",
                tailAlias = type.toType().typeName,
                RelationKind.Aggregation
            )
        }
    }

    class InheritanceBuilder(val child: KSClassDeclaration, val parent: KSClassDeclaration, override val options: Options? = null) : Builder {
        override fun build(): ElementRelation {
            return SimpleElementRelation(
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
            val filedType = classAttribute.attributeType

            return when {
                !filedType.isGeneric -> {
                    val typeAlias = filedType.fullQualifiedName.replace(".", "_").trim('_')
                    SimpleElementRelation("$classNameAlias::$propertyName", typeAlias, RelationKind.Property)
                }

                filedType.isGeneric -> {
                    SuperGenericElementRelation("$classNameAlias::$propertyName", filedType, RelationKind.Property)
                }

                classAttribute.genericTypes.size == 1 -> {
                    val simpleTypeAlias = classAttribute.attributeType.typeName
                    val typeAlias = classAttribute.genericTypes.firstOrNull()?.type?.resolve()?.toType()?.fullQualifiedName?.replace(".", "_")?.trim('_')
                    if (typeAlias != null) {
                        SimpleElementRelation("$classNameAlias::$propertyName", typeAlias, RelationKind.Property, simpleTypeAlias)
                    } else {
                        val typeAliasOfGeneric = classAttribute.attributeType.fullQualifiedName.replace(".", "_").trim('_')
                        SimpleElementRelation("$classNameAlias::$propertyName", typeAliasOfGeneric, RelationKind.Property)
                    }
                }

                else -> {
                    val simpleTypeAlias = classAttribute.attributeType.typeName
                    val aliases = classAttribute.genericTypes.mapNotNull { it.type?.resolve()?.toType()?.fullQualifiedName?.replace(".", "_")?.trim('_') }
                    GenericElementRelation(
                        headAlias = "$classNameAlias::$propertyName",
                        diamondAlias = "d_${classNameAlias}_$propertyName",
                        tailAliases = aliases,
                        relationKind = RelationKind.Property,
                        text = simpleTypeAlias
                    )
                }
            }

        }
    }

    class FunctionBuilder(val classDeclaration: KSClassDeclaration, val classMethod: Method, override val options: Options? = null, val logger: KSPLogger? = null) : Builder {
        override fun build(): ElementRelation {
            val classNameAlias = classDeclaration.fullQualifiedName.replace(".", "_").trim('_')
            val propertyName = classMethod.functionName
            val typeAlias = classMethod.returnType.fullQualifiedName.replace(".", "_").trim('_')
            return SimpleElementRelation("$classNameAlias::$propertyName", typeAlias, RelationKind.Property)
        }
    }
}
