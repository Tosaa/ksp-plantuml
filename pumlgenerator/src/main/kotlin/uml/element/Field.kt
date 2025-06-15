package uml.element

import Options
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier

data class Field(val originalKSProperty: KSPropertyDeclaration, val showVisibility: Boolean = true) {
    val uniqueIdentifier: String
        get() = originalKSProperty.qualifiedName?.getQualifier() ?: (originalKSProperty.packageName.asString() + originalKSProperty.simpleName.asString())
    val attributeName: String
        get() = originalKSProperty.simpleName.asString()
    val attributeType: Type
        get() = originalKSProperty.type.resolve().toType()
    val attributeModifiers: List<String>
        get() = originalKSProperty.modifiers.map { it.toString() }
    val visibility = if (showVisibility) "${originalKSProperty.getVisibility().pumlVisibility} " else ""

    fun render(): String {
        val modifiers = if (attributeModifiers.contains(Modifier.JAVA_STATIC.toString())) "{static} " else ""
        return "$visibility$modifiers$attributeName : ${attributeType.typeName}"
    }
}

fun KSPropertyDeclaration.toField(options: Options?): Field {
    return Field(this, options?.showVisibilityModifiers == true)
}