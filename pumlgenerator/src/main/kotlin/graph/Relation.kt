package graph

import Notation
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

    /**
     * Multiplicity at the end of the relation, e.g. `0..1` or `*`. Empty if the relation has no multiplicity.
     */
    open val toMultiplicity: String = ""

    /**
     * The end of the relation as it is written into the plantuml file: [toAlias], with the quoted [toMultiplicity] in front of it in the notation of UML.
     */
    fun target(notation: Notation): String = if (notation == Notation.UML && toMultiplicity.isNotEmpty()) "\"$toMultiplicity\" $toAlias" else toAlias

    override fun toString(): String {
        return "Relation(fromAlias=$fromAlias, fromAliasDetail=$fromAliasDetail, toAlias=$toAlias, toAliasDetail=$toAliasDetail, relationKind=$relationKind)"
    }
}

/**
 * Relation to indicate that a child inherits a parent class or that a child interface extends a parent interface.
 */
class InheritanceRelation(val child: KSClassDeclaration, val parent: KSClassDeclaration) : Relation(RelationKind.Inheritance) {
    override val fromAlias: String = child.fullQualifiedName.toPumlAlias()
    override val toAlias: String = parent.fullQualifiedName.toPumlAlias()
}

/**
 * Relation to indicate that a class or an object implements an interface.
 */
class RealizationRelation(val child: KSClassDeclaration, val parent: KSClassDeclaration) : Relation(RelationKind.Realization) {
    override val fromAlias: String = child.fullQualifiedName.toPumlAlias()
    override val toAlias: String = parent.fullQualifiedName.toPumlAlias()
}

/**
 * Relation to indicate that a class has a variable of a type of another class.
 * The multiplicity is `0..1` if the variable is nullable and `1` otherwise.
 */
class PropertyRelation(val classDeclaration: KSClassDeclaration, val classAttribute: Field, val fieldType: Type = classAttribute.attributeType, val logger: KSPLogger? = null) : Relation(RelationKind.Property) {
    val classNameAlias = classDeclaration.fullQualifiedName.toPumlAlias()
    val propertyName = classAttribute.attributeName


    override val fromAlias: String
        get() = classNameAlias

    override val fromAliasDetail: String
        get() = propertyName

    override val toAlias: String = fieldType.fullQualifiedName.toPumlAlias()

    override val toMultiplicity: String = if (fieldType.isNullable) "0..1" else "1"
}

/**
 * Relation to indicate that a class has a variable that holds any number of instances of another class.
 * E.g. collections like List<T> or Array<T>
 */
class CollectionPropertyRelation(val classDeclaration: KSClassDeclaration, val classAttribute: Field, val fieldType: Type, val logger: KSPLogger? = null) : Relation(RelationKind.CollectionProperty) {
    val classNameAlias = classDeclaration.fullQualifiedName.toPumlAlias()
    val propertyName = classAttribute.attributeName

    override val fromAlias: String
        get() = classNameAlias

    override val fromAliasDetail: String
        get() = propertyName

    override val toAlias: String = fieldType.fullQualifiedName.toPumlAlias()

    override val toMultiplicity: String = "*"
}

/**
 * Relation to indicate that a class has an indirect variable of a type of another class.
 * Its relation is indirect because the type of the variable refers to the other class as type argument.
 * E.g. Result<T>
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
 * Relation to indicate that a class has a function that returns a type of another class.
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

/**
 * Relation to indicate that a class has a function with a parameter of a type of another class.
 * E.g. `fun onEvent(event: Event)`
 */
class ParameterRelation(val classDeclaration: KSClassDeclaration, val classMethod: Method, val parameterType: Type, val logger: KSPLogger? = null) : Relation(RelationKind.Parameter) {
    val classNameAlias = classDeclaration.fullQualifiedName.toPumlAlias()
    val propertyName = classMethod.functionName

    override val fromAlias: String
        get() = classNameAlias
    override val fromAliasDetail: String
        get() = propertyName
    override val toAlias: String = parameterType.fullQualifiedName.toPumlAlias()
}

/**
 * Relation to indicate that a class has a function with a parameter that refers indirectly to a type of another class.
 * Its relation is indirect because the type of the parameter refers to the other class as type argument.
 * E.g. `fun onEvents(events: List<Event>)` or `fun onEvent(callback: (Event) -> Unit)`
 */
class IndirectParameterRelation(val classDeclaration: KSClassDeclaration, val classMethod: Method, val parameterType: Type, val logger: KSPLogger? = null) : Relation(RelationKind.IndirectParameter) {
    val classNameAlias = classDeclaration.fullQualifiedName.toPumlAlias()
    val propertyName = classMethod.functionName

    override val fromAlias: String
        get() = classNameAlias
    override val fromAliasDetail: String
        get() = propertyName
    override val toAlias: String = parameterType.fullQualifiedName.toPumlAlias()
}
