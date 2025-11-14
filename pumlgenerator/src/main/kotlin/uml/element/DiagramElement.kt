package uml.element

import ensureStartsWith
import uml.ElementKind
import uml.Renderable

sealed class DiagramElement(val elementName: String, val elementAlias: String, val uniqueIdentifier: String, val attributes: List<Field>, val functions: List<Method>, val isShell: Boolean) : Renderable {

    abstract val elementKind: ElementKind

    val comment: String = "'$uniqueIdentifier"

    open fun getContent(indent: String): String {
        val shellString = if (isShell) shellString else ""
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
$attributesString
$functionsString
        """
    }


    override fun render(): String {
        return """
    ${comment.ensureStartsWith('\'')}
    ${elementKind.kind} "$elementName" as $elementAlias ${elementKind.kindExtra}{
        ${getContent("$INDENT$INDENT").trim()}
    }
"""
    }

    override fun toString(): String {
        return "AbstractElement(elementName=$elementName, elementAlias=$elementAlias, uniqueIdentifier=$uniqueIdentifier, attributes=${attributes.map { it.uniqueIdentifier }}, functions=${functions.map { it.uniqueIdentifier }}, isShell=$isShell, elementKind=$elementKind)"
    }

    companion object {
        val INDENT = "\t"
        val PROPERTY_INDENT = "\t\t"
        val FUNCTION_INDENT = "\t\t"
        val shellString = "...\n=="
    }
}