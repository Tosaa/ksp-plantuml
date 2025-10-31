package uml.element

import Options
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeAlias
import isValid
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
    isShell: Boolean
) : DiagramElement(elementName, elementAlias, uniqueIdentifier, attributes, functions, isShell) {

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

    class Builder(val typeAlias: KSTypeAlias, clazz: KSClassDeclaration, isShell: Boolean, options: Options, logger: KSPLogger?) : DiagramElementBuilder(clazz, isShell, options, logger) {

        val type: Type = typeAlias.type.resolve().toType()

        override val packageName: String
            get() = typeAlias.packageName.asString()

        override var isShell: Boolean
            get() = false
            set(value) {}

        override val fullQualifiedName: String
            get() = typeAlias.fullQualifiedName

        override val allProperties: List<KSPropertyDeclaration>
            get() = extensionProperties

        override val allFunctions: List<KSFunctionDeclaration>
            get() = extensionFunctions

        override fun build(): TypealiasElement? {
            return if (options.isValid(typeAlias, logger)) {
                TypealiasElement(
                    uniqueIdentifier = typeAlias.fullQualifiedName,
                    elementName = typeAlias.shortName,
                    elementAlias = typeAlias.fullQualifiedName.replace(".", "_").trim('_'),
                    originalName = type.typeName,
                    attributes = allProperties.map { it.toField(options) },
                    functions = allFunctions.map { it.toMethod(options) },
                    isShell = isShell
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
