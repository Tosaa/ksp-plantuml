package uml.element

import Options
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import filterFunctionsByOptions
import filterPropertiesByOptions
import isValid
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
) : DiagramElement(elementName, elementAlias, uniqueIdentifier, attributes, functions, isShell) {

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

    class Builder(clazz: KSClassDeclaration, isShell: Boolean, options: Options, logger: KSPLogger?) : DiagramElementBuilder(clazz, isShell, options, logger) {

        fun getEntries(
            enumKSClassDeclaration: KSClassDeclaration
        ): List<String> {
            return enumKSClassDeclaration.declarations
                .filterIsInstance<KSClassDeclaration>()
                .filter { it.classKind == ClassKind.ENUM_ENTRY }
                .map { it.simpleName.asString() }
                .toList()
        }


        // For Enums we need to access allProperties to also get the ones from Enum<T>
        override val allProperties: List<KSPropertyDeclaration>
            get() = buildList {
                if (!isShell) {
                    val validCompanionObjectProperties = getCompanionObjectProperties(clazz).asSequence().filterPropertiesByOptions(clazz, options, logger) ?: emptySequence()
                    addAll(validCompanionObjectProperties)

                    val validProperties = clazz.getAllProperties().filterPropertiesByOptions(clazz, options, logger)
                    addAll(validProperties)

                    val otherProperties = clazz.declarations.filterIsInstance<KSPropertyDeclaration>()
                        .filterNot { it in validProperties }
                        .filterPropertiesByOptions(clazz, options, logger)
                    addAll(otherProperties)
                }

                addAll(extensionProperties)
            }.distinct()

        // For Enums we need to access allProperties to also get the ones from Enum<T>
        override val allFunctions: List<KSFunctionDeclaration>
            get() = buildList {
                if (!isShell) {
                    val validCompanionObjectFunctions = getCompanionObjectFunctions(clazz).asSequence().filterFunctionsByOptions(clazz, options, logger) ?: emptySequence()
                    addAll(validCompanionObjectFunctions)

                    val validFunctions = clazz.getAllFunctions().filterFunctionsByOptions(clazz, options, logger)
                    addAll(validFunctions)

                    val otherFunctions = clazz.declarations.filterIsInstance<KSFunctionDeclaration>()
                        .filterNot { it in validFunctions }
                        .filterFunctionsByOptions(clazz, options, logger)
                    addAll(otherFunctions)
                }

                addAll(extensionFunctions)
            }

        override fun build(): EnumElement? {
            return if (options.isValid(clazz, logger)) {
                EnumElement(
                    uniqueIdentifier = clazz.fullQualifiedName,
                    elementName = clazz.className,
                    elementAlias = clazz.fullQualifiedName.replace(".", "_").trim('_'),
                    attributes = allProperties.map { it.toField(options) },
                    functions = allFunctions.map { it.toMethod(options) },
                    members = getEntries(clazz),
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
