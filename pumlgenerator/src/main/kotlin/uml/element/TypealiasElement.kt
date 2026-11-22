package uml.element

import Notation
import Options
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeAlias
import isValid
import toPumlAlias
import uml.ElementKind
import uml.fullQualifiedName
import uml.shortName

/**
 * Expected layout:
 * class "name" as package_name << (A, #FF7700) alias>> {
 *  Type alias of "original name"
 *  ==
 *  Extension functions
 * }
 *
 */
class TypealiasElement(
    uniqueIdentifier: String,
    elementName: String,
    elementAlias: String,
    val originalName: String,
    attributes: List<Field>,
    functions: List<Method>,
    isShell: Boolean,
    notation: Notation
) : DiagramElement(elementName, elementAlias, uniqueIdentifier, attributes, functions, isShell, notation) {

    override val elementKind: ElementKind = ElementKind.ALIAS

    override fun getContent(indent: String): String {
        val typealiasDisclaimer = "${indent}TypeAlias of $originalName"
        val attributesString = attributes
            .takeIf { it.isNotEmpty() }
            ?.let { it.joinToString(separator = "\n") { "$indent${it.render()}" } }
            ?: ""
        val functionsString = functions
            .takeIf { it.isNotEmpty() }
            ?.let { it.joinToString(separator = "\n") { "$indent${it.render()}" } }
            ?: ""
        return """
$typealiasDisclaimer
$indent==
$attributesString
$functionsString
"""
    }

    // An alias renders only the extensions that are declared for it.
    // The aliased class is passed as shell to the base class, so that its members are not resolved.
    class Builder(val typeAlias: KSTypeAlias, clazz: KSClassDeclaration, options: Options, logger: KSPLogger?) : DiagramElementBuilder(clazz, true, options, logger) {

        val type: Type = typeAlias.type.resolve().toType()

        override val packageName: String = typeAlias.packageName.asString()

        override val isShell: Boolean = false

        override val fullQualifiedName: String = typeAlias.fullQualifiedName

        override fun buildElement(): TypealiasElement? {
            return if (options.isValid(typeAlias, logger)) {
                TypealiasElement(
                    uniqueIdentifier = typeAlias.fullQualifiedName,
                    elementName = typeAlias.shortName,
                    elementAlias = typeAlias.fullQualifiedName.toPumlAlias(),
                    originalName = type.typeName,
                    attributes = allProperties,
                    functions = allFunctions,
                    isShell = isShell,
                    notation = options.notation
                )
            } else {
                null
            }
        }

        override fun toString(): String {
            return "TypeAliasBuilder(alias=${typeAlias}, clazz=${clazz.fullQualifiedName})"
        }
    }
}
