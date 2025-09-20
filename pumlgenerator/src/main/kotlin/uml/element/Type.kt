package uml.element

import Options
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSType
import i
import isValid

val PRIMITIVE_NAMES: List<String> = listOf(
    "String",
    "Int",
    "Long",
    "Short",
    "Boolean",
    "Byte",
)

open class Type(
    val originalKSType: KSType? = null,
    val typeName: String
) {
    val fullQualifiedName: String
        get() = if (originalKSType == null) {
            ""
        } else {
            "${originalKSType.declaration.qualifiedName?.getQualifier() ?: originalKSType.declaration.packageName.asString()}.${originalKSType.declaration.simpleName.asString()}"
        }

    val isCollection: Boolean
        get() = originalKSType != null && originalKSType.declaration.packageName.asString().startsWith("kotlin.collections")

    val isPrimitive: Boolean
        get() = originalKSType == null || originalKSType.declaration.let {
            it.packageName.asString().contentEquals("kotlin") &&
                    it.simpleName.asString() in PRIMITIVE_NAMES
        }

    val isGeneric = (originalKSType?.arguments?.size ?: 0) > 0
    val genericTypes = originalKSType?.arguments?.mapNotNull { it.type?.resolve()?.toType() } ?: emptyList()
}

class ReservedType(
    originalKSType: KSType? = null,
    typeName: String
) : Type(originalKSType, typeName)

/**
 * flatResolve is used to resolve all referenced types of a type.
 * E.g.
 * Result<Pair<Int,String> consists of types: Result, Pair, Int and String.
 * Result is the first level type and holds Pair as second level type. Int and String are third level types.
 * E.g.
 * List<Foo> consists of types: List, Foo.
 * List is the first level type and holds Foo as second level type.
 */
fun Type.flatResolve(options: Options, logger: KSPLogger? = null, level: Int = 0): Set<Pair<Type, Int>> {
    logger.i { "flatResolve(): $this" }
    val resolved = buildSet {
        when {
            isCollection -> {
                logger.i { "flatResolve(): resolve collection: ${genericTypes.joinToString()}" }
                genericTypes.forEach {
                    addAll(it.flatResolve(options = options, logger = logger, level + 1))
                }
            }

            genericTypes.isNotEmpty() -> {
                logger.i { "flatResolve(): resolve and add: ${genericTypes.joinToString()}" }
                if (options.isValid(type = this@flatResolve.originalKSType, logger = logger)) {
                    add(this@flatResolve to level)
                }
                genericTypes.forEach {
                    addAll(it.flatResolve(options = options, logger = logger, level + 1))
                }
            }

            isPrimitive -> {
                logger.i { "flatResolve(): $this is primitive -> Ignore" }
                // Do nothing
            }

            else -> {
                logger.i { "flatResolve(): resolved: $this" }
                add(this@flatResolve to level)
            }
        }
    }
    return resolved.filterNot { it.first is ReservedType || it.first.isPrimitive }.toSet()
}

fun KSType.toType(): Type {
    val genericTypes = this.arguments.mapNotNull { it.type?.resolve()?.toType() }
    val generic = if (genericTypes.isNotEmpty()) genericTypes.joinToString(prefix = "<", postfix = ">", separator = ",") { it.typeName } else ""
    val optionalIndicator = if (this.isMarkedNullable) {
        "?"
    } else {
        ""
    }

    if (this.declaration.qualifiedName?.asString()?.startsWith("kotlin") == true){
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
