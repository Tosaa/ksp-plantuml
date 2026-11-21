package graph

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import toPumlAlias
import uml.element.Field
import uml.element.Method
import uml.element.Type
import uml.fullQualifiedName


sealed class Relation(val relationKind: RelationKind) {
    abstract val fromAlias: String
    open val fromAliasDetail: String = "" // Mostly unused but interesting for debugging
    abstract val toAlias: String
    open val toAliasDetail: String = "" // Mostly unused but interesting for debugging

    override fun toString(): String {
        return "Relation(fromAlias=$fromAlias, fromAliasDetail=$fromAliasDetail, toAlias=$toAlias, toAliasDetail=$toAliasDetail, relationKind=$relationKind)"
    }
}

/**
 * Relation to indicate that a child inherits a parent class.
 */
class InheritanceRelation(val child: KSClassDeclaration, val parent: KSClassDeclaration) : Relation(RelationKind.Inheritance) {
    override val fromAlias: String = child.fullQualifiedName.toPumlAlias()
    override val toAlias: String = parent.fullQualifiedName.toPumlAlias()
}

/**
 * Relation to indicate that a class has a variable of a type of another class.
 */
class PropertyRelation(val classDeclaration: KSClassDeclaration, val classAttribute: Field, val fieldType: Type = classAttribute.attributeType, val logger: KSPLogger? = null) : Relation(RelationKind.Property) {
    val classNameAlias = classDeclaration.fullQualifiedName.toPumlAlias()
    val propertyName = classAttribute.attributeName


    override val fromAlias: String
        get() = classNameAlias

    override val fromAliasDetail: String
        get() = propertyName

    override val toAlias: String = fieldType.fullQualifiedName.toPumlAlias()
}

/**
 * Relation to indicate that a class has an indirect variable of a type of another class.
 * Its relation is indirect because the class holds a variable which holds the type of the other class.
 * E.g. Result<T> or collections like List<T>
 */
class IndirectPropertyRelation(val classDeclaration: KSClassDeclaration, val classAttribute: Field, val fieldType: Type = classAttribute.attributeType, val logger: KSPLogger? = null) : Relation(RelationKind.IndirectProperty) {
    val classNameAlias = classDeclaration.fullQualifiedName.toPumlAlias()
    val propertyName = classAttribute.attributeName

    override val fromAlias: String
        get() = classNameAlias

    override val fromAliasDetail: String
        get() = propertyName

    override val toAlias: String = fieldType.fullQualifiedName.toPumlAlias()
}

/**
 * Relation to indicate that a class has a function that returns indirectly a type of another class.
 */
class FunctionRelation(val classDeclaration: KSClassDeclaration, val classMethod: Method, val returnType: Type = classMethod.returnType, val logger: KSPLogger? = null) : Relation(RelationKind.Function) {
    val classNameAlias = classDeclaration.fullQualifiedName.toPumlAlias()
    val propertyName = classMethod.functionName

    override val fromAlias: String
        get() = classNameAlias
    override val fromAliasDetail: String
        get() = propertyName
    override val toAlias: String = returnType.fullQualifiedName.toPumlAlias()
}


/**
 * Relation to indicate that a class has a function that returns indirectly a type of another class.
 * Its relation is indirect because the returned class holds a variable, which is of type of the other class.
 * E.g. Result<T> or collections like List<T>
 */
class IndirectFunctionRelation(val classDeclaration: KSClassDeclaration, val classMethod: Method, val returnType: Type = classMethod.returnType, val logger: KSPLogger? = null) : Relation(RelationKind.IndirectFunction) {
    val classNameAlias = classDeclaration.fullQualifiedName.toPumlAlias()
    val propertyName = classMethod.functionName

    override val fromAlias: String
        get() = classNameAlias
    override val fromAliasDetail: String
        get() = propertyName
    override val toAlias: String = returnType.fullQualifiedName.toPumlAlias()
}
