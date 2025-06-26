package uml.element

import Options
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference

data class Method(val originalKSFunctionDeclaration: KSFunctionDeclaration, val showVisibility: Boolean, val markExtension: Boolean) {
    val uniqueIdentifier: String
        get() = originalKSFunctionDeclaration.qualifiedName?.getQualifier() ?: (originalKSFunctionDeclaration.packageName.asString() + originalKSFunctionDeclaration.simpleName.asString())
    val functionName: String
        get() = originalKSFunctionDeclaration.simpleName.asString()
    val parameter: List<Type>
        get() = originalKSFunctionDeclaration.parameters.map { it.type.resolve().toType() }
    val returnType: Type
        get() = originalKSFunctionDeclaration.returnType?.resolve()?.toType() ?: Type.Unit

    val modifiers = mutableListOf<String>().apply {
        val isExtensionFunction = originalKSFunctionDeclaration.extensionReceiver != null
        val isParentCompanion = (originalKSFunctionDeclaration.parent as? KSClassDeclaration)?.isCompanionObject == true
        val isExtensionReceiverCompanion = (originalKSFunctionDeclaration.extensionReceiver?.resolve()?.declaration as? KSClassDeclaration)?.isCompanionObject == true
        val isCompanionFunction = isParentCompanion || isExtensionReceiverCompanion
        if (isCompanionFunction) {
            add("{static}")
        }
        if (markExtension && isExtensionFunction) {
            add("<ext>")
        }
        if (originalKSFunctionDeclaration.modifiers.contains(com.google.devtools.ksp.symbol.Modifier.SUSPEND)) {
            add("suspend")
        }
    }

    val visibility = if (showVisibility) "${originalKSFunctionDeclaration.getVisibility().pumlVisibility} " else ""

    fun render(): String {
        val modifiers = modifiers.takeIf { it.isNotEmpty() }?.joinToString(" ", "", " ") ?: ""
        return "$visibility$modifiers${functionName}(${parameter.joinToString(", ") { it.typeName }}) : ${returnType.typeName}"
    }
}

fun KSFunctionDeclaration.toMethod(options: Options?): Method {
    return Method(this, options?.showVisibilityModifiers == true, options?.markExtensions == true)
}