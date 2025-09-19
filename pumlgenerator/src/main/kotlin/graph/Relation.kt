package graph

import Options
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import uml.element.Field
import uml.element.Method
import uml.element.Type
import uml.fullQualifiedName

abstract class Relation {
    abstract val fromAlias: String
    open val fromAliasDetail: String = ""
    abstract val toAlias: String
    open val toAliasDetail: String = ""
    open val options: Options? = null
    abstract val relationKind: RelationKind

    final fun render(): String {
        val from = if (fromAliasDetail.isNotEmpty()) {
            "$fromAlias::$fromAliasDetail"
        } else {
            fromAlias
        }
        val to = if (toAliasDetail.isNotEmpty()) {
            "$toAlias::$toAliasDetail"
        } else {
            toAlias
        }
        return if (relationKind == RelationKind.Inheritance) {
            """$to ${relationKind.reversedArrow} $from""".trim()
        } else {
            """$from ${relationKind.arrow} $to""".trim()
        }

    }

    override fun toString(): String {
        return "Relation(fromAlias=$fromAlias, fromAliasDetail=$fromAliasDetail, toAlias=$toAlias, toAliasDetail=$toAliasDetail, relationKind=$relationKind)"
    }
}

class InheritanceRelation(val child: KSClassDeclaration, val parent: KSClassDeclaration, override val options: Options? = null) : Relation() {
    override val fromAlias: String
        get() = child.fullQualifiedName.replace(".", "_").trim('_')
    override val toAlias: String
        get() = parent.fullQualifiedName.replace(".", "_").trim('_')
    override val relationKind: RelationKind = RelationKind.Inheritance
}

class PropertyRelation(val classDeclaration: KSClassDeclaration, val classAttribute: Field, val fieldType: Type = classAttribute.attributeType, override val options: Options? = null, val logger: KSPLogger? = null) : Relation() {
    val classNameAlias = classDeclaration.fullQualifiedName.replace(".", "_").trim('_')
    val propertyName = classAttribute.attributeName


    override val fromAlias: String
        get() = classNameAlias

    override val fromAliasDetail: String
        get() = propertyName

    override val toAlias: String
        get() = fieldType.fullQualifiedName.replace(".", "_").trim('_')

    override val relationKind: RelationKind
        get() = RelationKind.Property
}

class IndirectPropertyRelation(val classDeclaration: KSClassDeclaration, val classAttribute: Field, val fieldType: Type = classAttribute.attributeType, override val options: Options? = null, val logger: KSPLogger? = null) : Relation() {
    val classNameAlias = classDeclaration.fullQualifiedName.replace(".", "_").trim('_')
    val propertyName = classAttribute.attributeName

    override val fromAlias: String
        get() = classNameAlias

    override val fromAliasDetail: String
        get() = propertyName

    override val toAlias: String
        get() = fieldType.fullQualifiedName.replace(".", "_").trim('_')

    override val relationKind: RelationKind
        get() = RelationKind.IndirectProperty
}


class FunctionRelation(val classDeclaration: KSClassDeclaration, val classMethod: Method, val returnType: Type = classMethod.returnType, override val options: Options? = null, val logger: KSPLogger? = null) : Relation() {
    val classNameAlias = classDeclaration.fullQualifiedName.replace(".", "_").trim('_')
    val propertyName = classMethod.functionName

    override val fromAlias: String
        get() = classNameAlias
    override val fromAliasDetail: String
        get() = propertyName
    override val toAlias: String
        get() = returnType.fullQualifiedName.replace(".", "_").trim('_')
    override val relationKind: RelationKind
        get() = RelationKind.Property
}

class IndirectFunctionRelation(val classDeclaration: KSClassDeclaration, val classMethod: Method, val returnType: Type = classMethod.returnType, override val options: Options? = null, val logger: KSPLogger? = null) : Relation() {
    val classNameAlias = classDeclaration.fullQualifiedName.replace(".", "_").trim('_')
    val propertyName = classMethod.functionName

    override val fromAlias: String
        get() = classNameAlias
    override val fromAliasDetail: String
        get() = propertyName
    override val toAlias: String
        get() = returnType.fullQualifiedName.replace(".", "_").trim('_')
    override val relationKind: RelationKind
        get() = RelationKind.Property
}
