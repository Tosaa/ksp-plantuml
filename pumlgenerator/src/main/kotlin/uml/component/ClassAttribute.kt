package uml.component

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier

data class ClassAttribute(val uniqueIdentifier: String, val attributeName: String, val attributeType: Type, val attributeModifiers: List<String>) {
    fun render(): String {
        val modifiers = if (attributeModifiers.contains(Modifier.JAVA_STATIC.toString())) "{static} " else ""
        return "$modifiers$attributeName : ${attributeType.typeAlias}"
    }
}

fun KSPropertyDeclaration.toAttribute(): ClassAttribute {
    return ClassAttribute(
        uniqueIdentifier = this.qualifiedName?.getQualifier() ?: (this.packageName.asString() + this.simpleName.asString()),
        attributeName = this.simpleName.asString(),
        attributeType = this.type.resolve().toType(),
        attributeModifiers = this.modifiers.map { it.toString() })
}