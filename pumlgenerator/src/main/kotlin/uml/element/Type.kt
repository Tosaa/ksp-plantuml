package uml.element

import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSType
import i

private const val ARRAY_NAME = "kotlin.Array"

val PRIMITIVE_NAMES: List<String> = listOf(
    "String",
    "Int",
    "Long",
    "Short",
    "Boolean",
    "Byte",
)

class FunctionType(
    originalKSType: KSType? = null,
    val argumentTypes: List<Type> = emptyList(),
    val returnType: Type? = null,
) : Type(
    originalKSType,
    "${argumentTypes.joinToString(prefix = "(", postfix = ")", separator = ", ") { it.typeName }} -> ${(returnType ?: ReservedType(null, "Unit")).typeName}"
)

open class Type(
    val originalKSType: KSType? = null,
    val typeName: String
) {
    val fullQualifiedName: String = if (originalKSType == null) {
        ""
    } else {
        "${originalKSType.declaration.qualifiedName?.getQualifier() ?: originalKSType.declaration.packageName.asString()}.${originalKSType.declaration.simpleName.asString()}"
    }

    val isCollection: Boolean = originalKSType != null && originalKSType.declaration.packageName.asString().startsWith("kotlin.collections")

    val isPrimitive: Boolean = originalKSType == null || originalKSType.declaration.let {
        it.packageName.asString().contentEquals("kotlin") &&
                it.simpleName.asString() in PRIMITIVE_NAMES
    }

    val isNullable: Boolean = originalKSType?.isMarkedNullable == true

    // The relations are checked against the options after the last processing round, when the declaration must not be accessed anymore.
    // Therefore, the values that are checked are taken from the declaration when the Type is created.
    val isPublic: Boolean = originalKSType?.declaration?.isPublic() == true
    val isInternal: Boolean = originalKSType?.declaration?.isInternal() == true
    val isPrivate: Boolean = originalKSType?.declaration?.isPrivate() == true
    val packageName: String = originalKSType?.declaration?.packageName?.asString() ?: ""

    val isGeneric = (originalKSType?.arguments?.size ?: 0) > 0
    val genericTypes = originalKSType?.arguments?.mapNotNull { it.type?.resolve()?.toType() } ?: emptyList()

    // The KSType is not part of the description, since it must not be accessed after its processing round
    override fun toString(): String {
        return "Type(typeName=$typeName)"
    }
}

class ReservedType(
    originalKSType: KSType? = null,
    typeName: String
) : Type(originalKSType, typeName)

/**
 * A type that is referenced by another type, see [flatResolve].
 *
 * @param level 0 for the type itself, increased by one for every type that wraps it
 * @param isCollectionElement `true` if the type is wrapped by collections or arrays only, like `Foo` in `List<Foo>` or `Map<String, List<Foo>>`
 */
data class ResolvedType(val type: Type, val level: Int, val isCollectionElement: Boolean)

/**
 * flatResolve is used to resolve all referenced types of a type.
 * E.g.
 * Result<Pair<Int,String> consists of types: Result, Pair, Int and String.
 * Result is the first level type and holds Pair as second level type. Int and String are third level types.
 * E.g.
 * List<Foo> consists of types: List, Foo.
 * List is the first level type and holds Foo as second level type.
 */
fun Type.flatResolve(logger: KSPLogger? = null, level: Int = 0, isCollectionElement: Boolean = false): Set<ResolvedType> {
    logger.i { "flatResolve(): $this" }
    // The types that are wrapped by this type are elements of a collection, if this type and all types that wrap it are collections or arrays
    val wrapsCollectionElements = (isCollection || fullQualifiedName == ARRAY_NAME) && (level == 0 || isCollectionElement)
    val resolved = buildSet {
        when {
            isCollection -> {
                logger.i { "flatResolve(): resolve collection: ${genericTypes.joinToString()}" }
                genericTypes.forEach {
                    addAll(it.flatResolve(logger = logger, level + 1, wrapsCollectionElements))
                }
            }

            genericTypes.isNotEmpty() -> {
                logger.i { "flatResolve(): resolve and add: ${genericTypes.joinToString()}" }
                if (this@flatResolve.fullQualifiedName.isNotEmpty()) {
                    add(ResolvedType(this@flatResolve, level, isCollectionElement))
                }
                genericTypes.forEach {
                    addAll(it.flatResolve(logger = logger, level + 1, wrapsCollectionElements))
                }
            }

            isPrimitive -> {
                logger.i { "flatResolve(): ${this@flatResolve} is primitive -> Ignore" }
                // Do nothing
            }

            else -> {
                logger.i { "flatResolve(): resolved: ${this@flatResolve}" }
                add(ResolvedType(this@flatResolve, level, isCollectionElement))
            }
        }
    }
    return resolved.filterNot { it.type is ReservedType || it.type.isPrimitive }.toSet()
}

fun KSType.toType(): Type {
    if (isFunctionType) {
        return FunctionType(
            this,
            arguments.dropLast(1).mapNotNull { it.type?.resolve()?.toType() },
            arguments.lastOrNull()?.type?.resolve()?.toType()
        )
    }

    val genericTypes = this.arguments.mapNotNull { it.type?.resolve()?.toType() }
    val generic = if (genericTypes.isNotEmpty()) genericTypes.joinToString(prefix = "<", postfix = ">", separator = ",") { it.typeName } else ""
    val optionalIndicator = if (this.isMarkedNullable) {
        "?"
    } else {
        ""
    }

    if (this.declaration.qualifiedName?.asString()?.startsWith("kotlin") == true) {
        return ReservedType(
            this,
            "${this.declaration.simpleName.asString()}$generic$optionalIndicator"
        )
    }

    if (this.declaration.qualifiedName?.asString()?.contentEquals("kotlin.Result") == true) {
        return ReservedType(
            this,
            "${this.declaration.simpleName.asString()}$generic$optionalIndicator"
        )
    }

    return Type(
        this,
        "${this.declaration.simpleName.asString()}$generic$optionalIndicator"
    )
}
