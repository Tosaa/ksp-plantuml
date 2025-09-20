package uml.element

import Options
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSType
import i
import isValid
import java.util.Collections.addAll

val PRIMITIVE_NAMES: List<String> = listOf(
    "String",
    "Int",
    "Long",
    "Short",
    "Boolean",
    "Byte",
)

data class Type(
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

    companion object {
        val Unit: Type = Type(null, "Unit")
        val Any: Type = Type(null, "Any")
        val Exception: Type = Type(null, "Exception")
    }
}

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
    return resolved.filterNot { it.first in listOf(Type.Unit, Type.Any, Type.Exception) || it.first.isPrimitive }.toSet()
}

fun KSType.toType(): Type {
    val genericTypes = this.arguments.mapNotNull { it.type?.resolve()?.toType() }
    val generic = if (genericTypes.isNotEmpty()) genericTypes.joinToString(prefix = "<", postfix = ">", separator = ",") { it.typeName } else ""
    val optionalIndicator = if (this.isMarkedNullable) {
        "?"
    } else {
        ""
    }
    if (this.declaration.qualifiedName?.asString()?.contentEquals("kotlin.Unit") == true) {
        return Type.Unit
    }
    if (this.declaration.qualifiedName?.asString()?.contentEquals("kotlin.Any") == true) {
        return Type.Any
    }
    if (this.declaration.qualifiedName?.asString()?.contentEquals("kotlin.Exception") == true) {
        return Type.Exception
    }
    return Type(
        this,
        "${this.declaration.simpleName.asString()}$generic$optionalIndicator"
    )
}
