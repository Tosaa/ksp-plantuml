package uml.element

import Options
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

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
        val isCompanionFunction = (originalKSFunctionDeclaration.parent as? KSClassDeclaration)?.isCompanionObject == true
        if (isCompanionFunction) {
            add("{static}")
        }
        if (markExtension && originalKSFunctionDeclaration.extensionReceiver != null){
            add("<ext>")
        }
        if (originalKSFunctionDeclaration.modifiers.contains(com.google.devtools.ksp.symbol.Modifier.SUSPEND)) {
            add("suspend")
        }
    }

    val visibility = if (showVisibility) "${originalKSFunctionDeclaration.getVisibility().pumlVisibility} " else ""

    fun render(): String {
        return "$visibility${modifiers.takeIf { it.isNotEmpty() }?.joinToString(" ", "", " ") ?: ""}${functionName}(${parameter.joinToString(", ") { it.typeName }}) : ${returnType.typeName}"
    }
}

fun KSFunctionDeclaration.toMethod(options: Options?): Method {
    return Method(this, options?.showVisibilityModifiers == true, options?.markExtensions == true)
}