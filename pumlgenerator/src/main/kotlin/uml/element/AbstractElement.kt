package uml.element

import uml.DiagramElement
import uml.ElementKind

abstract class AbstractElement(override val elementName: String, override val elementAlias: String, val uniqueIdentifier: String, val attributes: List<Field>, val functions: List<Method>, val isShell: Boolean) : DiagramElement() {
    override val comment: String = "'$uniqueIdentifier"
    override fun getContent(indent: String): String {
        val shellString = if (isShell) DiagramElement.shellString else ""
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

    override fun toString(): String {
        return "AbstractElement(elementName=$elementName, elementAlias=$elementAlias, uniqueIdentifier=$uniqueIdentifier, attributes=${attributes.map { it.uniqueIdentifier }}, functions=${functions.map { it.uniqueIdentifier }}, isShell=$isShell)"
    }
}