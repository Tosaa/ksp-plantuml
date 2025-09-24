package uml.element

import Options
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeAlias
import isValid
import uml.ElementKind
import uml.className
import uml.fullQualifiedName
import uml.shortName


class TypealiasElement(
    uniqueIdentifier: String,
    elementName: String,
    elementAlias: String,
    val originalName:String,
    attributes: List<Field>,
    functions: List<Method>,
    isShell: Boolean
) : AbstractElement(elementName, elementAlias, uniqueIdentifier, attributes, functions, isShell) {

    override val elementKind: ElementKind = ElementKind.ALIAS

    override fun getContent(indent: String): String {
        val typealiasDisclaimer = "TypeAlias of $originalName"
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
==
$attributesString
$functionsString
"""
    }

    class Builder(val typeAlias: KSTypeAlias, clazz: KSClassDeclaration, isShell: Boolean, options: Options, logger: KSPLogger?) : AbstractElementBuilder(clazz, isShell, options, logger) {

        override fun build(): TypealiasElement? {
            return if (options.isValid(clazz, logger)) {
                TypealiasElement(
                    uniqueIdentifier = typeAlias.fullQualifiedName,
                    elementName = typeAlias.shortName,
                    elementAlias = typeAlias.fullQualifiedName.replace(".", "_").trim('_'),
                    originalName = clazz.className,
                    attributes = allProperties.map { it.toField(options) },
                    functions = allFunctions.map { it.toMethod(options) },
                    isShell = isShell
                )
            } else {
                null
            }
        }

        override fun toString(): String {
            return "TypeAliasBuilder(clazz=${clazz.fullQualifiedName})"
        }
    }
}
