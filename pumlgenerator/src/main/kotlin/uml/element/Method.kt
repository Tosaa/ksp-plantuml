package uml.element

import Notation
import Options
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier
import uml.pumlVisibility

data class Method(val originalKSFunctionDeclaration: KSFunctionDeclaration, val showVisibility: Boolean, val markExtension: Boolean, val notation: Notation) {
    // Everything is resolved when the Method is created, since the declaration must not be accessed after its processing round
    val uniqueIdentifier: String = originalKSFunctionDeclaration.qualifiedName?.getQualifier() ?: (originalKSFunctionDeclaration.packageName.asString() + originalKSFunctionDeclaration.simpleName.asString())
    val functionName: String = originalKSFunctionDeclaration.simpleName.asString()
    val parameter: List<Type> = originalKSFunctionDeclaration.parameters.map { it.type.resolve().toType() }
    // The parameters as they are rendered: the type in the notation of plantuml,
    // "name : Type" followed by the multiplicity * for a vararg parameter in the notation of UML
    val parameterDescriptions: List<String> = when (notation) {
        Notation.PLANTUML -> parameter.map { it.typeName }
        Notation.UML -> originalKSFunctionDeclaration.parameters.zip(parameter).map { (ksParameter, type) ->
            val name = ksParameter.name?.asString()?.takeIf { it.isNotBlank() }?.let { "$it : " } ?: ""
            val multiplicity = if (ksParameter.isVararg) " [*]" else ""
            "$name${type.typeName}$multiplicity"
        }
    }
    val returnType: Type = originalKSFunctionDeclaration.returnType?.resolve()?.toType() ?: ReservedType(null, "Unresolved")

    val modifiers = mutableListOf<String>().apply {
        val isExtensionFunction = originalKSFunctionDeclaration.extensionReceiver != null
        val isParentCompanion = (originalKSFunctionDeclaration.parent as? KSClassDeclaration)?.isCompanionObject == true
        val isExtensionReceiverCompanion = (originalKSFunctionDeclaration.extensionReceiver?.resolve()?.declaration as? KSClassDeclaration)?.isCompanionObject == true
        val isJVMStatic = originalKSFunctionDeclaration.modifiers.contains(Modifier.JAVA_STATIC)
        val isCompanionFunction = isParentCompanion || isExtensionReceiverCompanion || isJVMStatic
        if (isCompanionFunction) {
            add("{static}")
        }
        if (markExtension && isExtensionFunction) {
            add(if (notation == Notation.UML) "<<ext>>" else "<ext>")
        }
        if (originalKSFunctionDeclaration.modifiers.contains(com.google.devtools.ksp.symbol.Modifier.SUSPEND)) {
            add(if (notation == Notation.UML) "<<suspend>>" else "suspend")
        }
    }

    val visibility = if (showVisibility) "${originalKSFunctionDeclaration.getVisibility().pumlVisibility(notation)} " else ""

    fun render(): String {
        val modifiers = modifiers.takeIf { it.isNotEmpty() }?.joinToString(" ", "", " ") ?: ""
        return "$visibility$modifiers${functionName}(${parameterDescriptions.joinToString(", ")}) : ${returnType.typeName}"
    }

    override fun toString(): String {
        return "Method(name=$functionName, returnType=${returnType.typeName})"
    }
}

fun KSFunctionDeclaration.toMethod(options: Options?): Method {
    return Method(this, options?.showVisibilityModifiers == true, options?.markExtensions == true, options?.notation ?: Notation.DEFAULT)
}