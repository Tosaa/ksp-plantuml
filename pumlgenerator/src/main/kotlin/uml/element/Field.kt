package uml.element

import Options
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier

data class Field(val originalKSProperty: KSPropertyDeclaration, val showVisibility: Boolean = true, val markExtension: Boolean) {
    val uniqueIdentifier: String
        get() = originalKSProperty.qualifiedName?.getQualifier() ?: (originalKSProperty.packageName.asString() + originalKSProperty.simpleName.asString())
    val attributeName: String
        get() = originalKSProperty.simpleName.asString()
    val attributeType: Type
        get() = originalKSProperty.type.resolve().toType()
    val attributeModifiers: List<String>
        get() = originalKSProperty.modifiers.map { it.toString() }
    val visibility = if (showVisibility) "${originalKSProperty.getVisibility().pumlVisibility} " else ""


    val modifiers = mutableListOf<String>().apply {
        val isCompanionFunction = (originalKSProperty.parent as? KSClassDeclaration)?.isCompanionObject == true
        if (isCompanionFunction) {
            add("{static}")
        }
        if (markExtension && originalKSProperty.extensionReceiver != null){
            add("<ext>")
        }
    }

    fun render(): String {
        val modifiers = modifiers.takeIf { it.isNotEmpty() }?.joinToString(" ", "", " ") ?: ""
        return "$visibility$modifiers$attributeName : ${attributeType.typeName}"
    }
}

fun KSPropertyDeclaration.toField(options: Options?): Field {
    return Field(this, options?.showVisibilityModifiers == true, options?.markExtensions == true)
}