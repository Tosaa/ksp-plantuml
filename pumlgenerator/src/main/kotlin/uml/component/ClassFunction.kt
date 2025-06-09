package uml.component

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier

data class ClassFunction(val uniqueIdentifier: String, val functionName: String, val parameter: List<Type>, val returnType: Type, val modifiers: List<String>) {
    fun render(): String {
        return "${modifiers.takeIf { it.isNotEmpty() }?.joinToString(" ", "", " ") ?: ""}${functionName}(${parameter.joinToString(", ") { it.typeAlias }}) : ${returnType.typeAlias}"
    }
}

fun KSFunctionDeclaration.toFunction(): ClassFunction {
    val isCompanionFunction = (this.parent as? KSClassDeclaration)?.isCompanionObject == true
    val shownModifiers = mutableListOf<String>()
    if (isCompanionFunction) {
        shownModifiers.add("{static}")
    }
    if (modifiers.contains(Modifier.SUSPEND)) {
        shownModifiers.add("suspend")
    }
    return ClassFunction(
        uniqueIdentifier = this.qualifiedName?.getQualifier() ?: (this.packageName.asString() + this.simpleName.asString()),
        functionName = this.simpleName.asString(),
        parameter = this.parameters.map { it.type.resolve().toType() },
        returnType = this.returnType?.resolve()?.toType() ?: Type.Unit,
        modifiers = shownModifiers
    )
}