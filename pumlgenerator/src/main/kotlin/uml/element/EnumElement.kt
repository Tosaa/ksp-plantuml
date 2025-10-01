package uml.element

import Options
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import isValid
import uml.DiagramElement
import uml.ElementKind
import uml.className
import uml.fullQualifiedName


class EnumElement(
    uniqueIdentifier: String,
    elementName: String,
    elementAlias: String,
    attributes: List<Field>,
    functions: List<Method>,
    val members: List<String>,
    isShell: Boolean
) : AbstractElement(elementName, elementAlias, uniqueIdentifier, attributes, functions, isShell) {

    override val elementKind: ElementKind = ElementKind.ENUM

    override fun getContent(indent: String): String {
        val shellString = if (isShell) DiagramElement.shellString else ""
        val membersString = members
            .takeIf { it.isNotEmpty() }
            ?.joinToString("\n", postfix = "\n${indent}__") { "$indent$it" }
            ?: ""
        val attributesString = attributes
            .takeIf { it.isNotEmpty() }
            ?.let { it.joinToString(separator = "\n") { "$indent${it.render()}" } }
            ?: ""
        val functionsString = functions
            .takeIf { it.isNotEmpty() }
            ?.let { it.joinToString(separator = "\n") { "$indent${it.render()}" } }
            ?: ""
        return """
$shellString
$membersString
$attributesString
$functionsString
"""
    }

    class Builder(clazz: KSClassDeclaration, isShell: Boolean, options: Options, logger: KSPLogger?) : AbstractElementBuilder<EnumElement>(clazz, isShell, options, logger) {

        override fun build(): EnumElement? {
            return if (options.isValid(clazz, logger)) {
                EnumElement(
                    uniqueIdentifier = clazz.fullQualifiedName,
                    elementName = clazz.className,
                    elementAlias = clazz.fullQualifiedName.replace(".", "_").trim('_'),
                    attributes = allProperties.map { it.toField(options) },
                    functions = allFunctions.map { it.toMethod(options) },
                    members = clazz.declarations
                        .mapNotNull { it as? KSClassDeclaration }
                        .filter { it.classKind == ClassKind.ENUM_ENTRY }
                        .map { it.simpleName.asString() }
                        .toList(),
                    isShell = isShell
                )
            } else {
                null
            }
        }

        override fun toString(): String {
            return "EnumBuilder(clazz=${clazz.fullQualifiedName})"
        }
    }
}
