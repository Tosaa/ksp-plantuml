package uml.element

import com.google.devtools.ksp.symbol.KSType

data class Type(
    val originalKSType: KSType? = null,
    val uniqueIdentifier: String,
    val typeName: String
) {
    val fullQualifiedName: String
        get() = uniqueIdentifier.replace(".", "_").trim('_')

    companion object {
        val Unit: Type = Type(null, "", "Unit")
    }
}


fun KSType.toType(): Type {
    val genericTypes = this.arguments.mapNotNull { it.type?.resolve()?.toType() }
    val generic = if (genericTypes.isNotEmpty()) genericTypes.joinToString(prefix = "<", postfix = ">", separator = ",") { it.typeName } else ""
    return Type(
        this,
        "${this.declaration.qualifiedName?.getQualifier() ?: this.declaration.packageName.asString()}.${this.declaration.simpleName.asString()}",
        "${this.declaration.simpleName.asString()}$generic"
    )
}
