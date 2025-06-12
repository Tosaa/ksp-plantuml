package uml.component

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier

data class ClassAttribute(val originalKSProperty: KSPropertyDeclaration) {
    val uniqueIdentifier: String
        get() = originalKSProperty.qualifiedName?.getQualifier() ?: (originalKSProperty.packageName.asString() + originalKSProperty.simpleName.asString())
    val attributeName: String
        get() = originalKSProperty.simpleName.asString()
    val attributeType: Type
        get() = originalKSProperty.type.resolve().toType()
    val attributeModifiers: List<String>
        get() = originalKSProperty.modifiers.map { it.toString() }

    fun render(): String {
        val modifiers = if (attributeModifiers.contains(Modifier.JAVA_STATIC.toString())) "{static} " else ""
        return "$modifiers$attributeName : ${attributeType.typeAlias}"
    }
}

fun KSPropertyDeclaration.toAttribute(): ClassAttribute {
    return ClassAttribute(this)
}