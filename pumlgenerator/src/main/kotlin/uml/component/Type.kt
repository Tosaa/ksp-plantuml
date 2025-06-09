package uml.component

import com.google.devtools.ksp.symbol.KSType

data class Type(val uniqueIdentifier: String, val typeAlias: String) {
    val fullQualifiedName: String
        get() = uniqueIdentifier.replace(".", "_")

    companion object {
        val Unit: Type = Type("", "Unit")
    }
}


fun KSType.toType(): Type {
    return Type(
        uniqueIdentifier = "${this.declaration.qualifiedName?.getQualifier() ?: this.declaration.packageName.asString()}.${this.declaration.simpleName.asString()}",
        typeAlias = this.declaration.simpleName.asString()
    )
}
