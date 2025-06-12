package uml.component

import com.google.devtools.ksp.symbol.KSType

data class Type(
    val originalKSType: KSType? = null,
    val uniqueIdentifier: String,
    val typeAlias: String
) {
    val fullQualifiedName: String
        get() = uniqueIdentifier.replace(".", "_").trim('_')

    companion object {
        val Unit: Type = Type(null, "", "Unit")
    }
}


fun KSType.toType(): Type {
    return Type(
        this,
        "${this.declaration.qualifiedName?.getQualifier() ?: this.declaration.packageName.asString()}.${this.declaration.simpleName.asString()}",
        this.declaration.simpleName.asString()
    )
}
