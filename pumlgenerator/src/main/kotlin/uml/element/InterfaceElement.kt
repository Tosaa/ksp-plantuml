package uml.element

import Options
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import filterFunctionsByOptions
import filterPropertiesByOptions
import isValid
import uml.DiagramElement
import uml.ElementKind
import uml.className
import uml.fullQualifiedName

data class InterfaceElement(val uniqueIdentifier: String, override val elementName: String, override val elementAlias: String, val attributes: List<Field>, val functions: List<Method>, val isSealedClass: Boolean, val isShell: Boolean) : DiagramElement() {
    override val comment: String = "'$uniqueIdentifier"
    override val elementKind: ElementKind = ElementKind.INTERFACE(isSealedClass)
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

    class Builder(override val clazz: KSClassDeclaration, override var isShell: Boolean, override val options: Options, val logger: KSPLogger?) : DiagramElement.Builder<InterfaceElement> {
        val companionObject = clazz.declarations.filter { it is KSClassDeclaration && it.isCompanionObject }.map { it as? KSClassDeclaration }.firstOrNull()

        override val extensionProperties: MutableList<KSPropertyDeclaration> = mutableListOf()


        override val allProperties: MutableList<KSPropertyDeclaration>
            get() = buildList {
                val validCompanionObjectProperties = companionObject?.getAllProperties()?.filterPropertiesByOptions(clazz, options, logger) ?: emptySequence()
                addAll(validCompanionObjectProperties)

                val validProperties = clazz.getAllProperties().filterPropertiesByOptions(clazz, options, logger)
                addAll(validProperties)

                val otherProperties = clazz.declarations.filterIsInstance<KSPropertyDeclaration>().filterNot { it in clazz.getAllProperties() }.filterPropertiesByOptions(clazz, options, logger)
                addAll(otherProperties)

                addAll(extensionProperties)
            }.toMutableList()

        override val extensionFunctions: MutableList<KSFunctionDeclaration> = mutableListOf()

        override val allFunctions: MutableList<KSFunctionDeclaration>
            get() = buildList {
                val validCompanionObjectFunctions = companionObject?.getAllFunctions()?.filterFunctionsByOptions(clazz, options, logger) ?: emptySequence()
                addAll(validCompanionObjectFunctions)

                val validFunctions = clazz.getAllFunctions().filterFunctionsByOptions(clazz, options, logger)
                addAll(validFunctions)

                val otherFunctions = clazz.declarations.filterIsInstance<KSFunctionDeclaration>().filterNot { it in clazz.getAllFunctions() }.filterFunctionsByOptions(clazz, options, logger)
                addAll(otherFunctions)

                addAll(extensionFunctions)
            }.toMutableList()

        override fun build(): InterfaceElement? {
            return if (options.isValid(clazz, logger)) {
                InterfaceElement(
                    uniqueIdentifier = clazz.fullQualifiedName,
                    elementName = clazz.className,
                    elementAlias = clazz.fullQualifiedName.replace(".", "_").trim('_'),
                    attributes = allProperties.map { it.toField(options) },
                    functions = allFunctions.map { it.toMethod(options) },
                    isSealedClass = clazz.modifiers.contains(Modifier.SEALED),
                    isShell = isShell
                )
            } else {
                null
            }
        }


        override fun toString(): String {
            return "InterfaceBuilder(clazz=${clazz.fullQualifiedName})"
        }
    }
}
