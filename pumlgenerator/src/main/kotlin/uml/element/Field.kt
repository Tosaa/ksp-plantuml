package uml.element

import Notation
import Options
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import uml.pumlVisibility


data class Field(val originalKSProperty: KSPropertyDeclaration, val showVisibility: Boolean = true, val markExtension: Boolean, val notation: Notation) {
    // Everything is resolved when the Field is created, since the declaration must not be accessed after its processing round
    val uniqueIdentifier: String = originalKSProperty.qualifiedName?.getQualifier() ?: (originalKSProperty.packageName.asString() + originalKSProperty.simpleName.asString())

    val attributeName: String = originalKSProperty.simpleName.asString()

    val attributeType: Type = originalKSProperty.type.resolve().toType()

    val attributeModifiers: List<String> = originalKSProperty.modifiers.map { it.toString() }

    val isCollection = attributeType.isCollection

    val isPrimitive = attributeType.isPrimitive

    val isJava = attributeType.fullQualifiedName.startsWith("java")

    val visibility = if (showVisibility) "${originalKSProperty.getVisibility().pumlVisibility(notation)} " else ""

    val genericTypes = attributeType.originalKSType?.arguments ?: emptyList()

    val modifiers = mutableListOf<String>().apply {
        val isParentCompanion = (originalKSProperty.parent as? KSClassDeclaration)?.isCompanionObject == true
        val isExtensionReceiverCompanion = (originalKSProperty.extensionReceiver?.resolve()?.declaration as? KSClassDeclaration)?.isCompanionObject == true
        val isJVMStatic = originalKSProperty.modifiers.contains(Modifier.JAVA_STATIC)
        val isCompanionFunction = isParentCompanion || isExtensionReceiverCompanion || isJVMStatic
        if (originalKSProperty.modifiers.contains(Modifier.ABSTRACT)) {
            add("{abstract}")
        }
        if (isCompanionFunction) {
            add("{static}")
        }
        if (markExtension && originalKSProperty.extensionReceiver != null) {
            add(if (notation == Notation.UML) "<<ext>>" else "<ext>")
        }
    }

    fun render(): String {
        val modifiers = modifiers.takeIf { it.isNotEmpty() }?.joinToString(" ", "", " ") ?: ""
        return "$visibility$modifiers$attributeName : ${attributeType.typeName}"
    }

    override fun toString(): String {
        return "Field(name=${attributeName}, type=${attributeType.typeName})"
    }
}

fun KSPropertyDeclaration.toField(options: Options?): Field {
    return Field(this, options?.showVisibilityModifiers == true, options?.markExtensions == true, options?.notation ?: Notation.DEFAULT)
}