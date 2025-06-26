package uml.element

import com.google.devtools.ksp.symbol.KSType

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


    companion object {
        val Unit: Type = Type(null, "Unit")
    }
}


fun KSType.toType(): Type {
    val genericTypes = this.arguments.mapNotNull { it.type?.resolve()?.toType() }
    val generic = if (genericTypes.isNotEmpty()) genericTypes.joinToString(prefix = "<", postfix = ">", separator = ",") { it.typeName } else ""
    val optionalIndicator = if (this.isMarkedNullable){"?"}else{""}
    return Type(
        this,
        "${this.declaration.simpleName.asString()}$generic$optionalIndicator"
    )
}
