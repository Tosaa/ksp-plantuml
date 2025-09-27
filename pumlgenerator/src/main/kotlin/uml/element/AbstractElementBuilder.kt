package uml.element

import Options
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import filterFunctionsByOptions
import filterPropertiesByOptions
import uml.DiagramElement
import uml.fullQualifiedName

abstract class AbstractElementBuilder(override val clazz: KSClassDeclaration, override var isShell: Boolean, override val options: Options, val logger: KSPLogger? = null) : DiagramElement.Builder<AbstractElement> {

    val companionObject = clazz.declarations.filter { it is KSClassDeclaration && it.isCompanionObject }.map { it as? KSClassDeclaration }.firstOrNull()

    override val fullQualifiedName: String
        get() = clazz.fullQualifiedName

    protected val extensionProperties: MutableList<KSPropertyDeclaration> = mutableListOf()

    override val allProperties: List<KSPropertyDeclaration>
        get() = buildList {
            if (!isShell) {
                val validCompanionObjectProperties = companionObject?.getAllProperties()?.filterPropertiesByOptions(clazz, options, logger) ?: emptySequence()
                addAll(validCompanionObjectProperties)

                val validProperties = clazz.getAllProperties().filterPropertiesByOptions(clazz, options, logger)
                addAll(validProperties)

                val otherProperties = clazz.declarations.filterIsInstance<KSPropertyDeclaration>().filterNot { it in clazz.getAllProperties() }.filterPropertiesByOptions(clazz, options, logger)
                addAll(otherProperties)
            }

            addAll(extensionProperties)
        }

    protected val extensionFunctions: MutableList<KSFunctionDeclaration> = mutableListOf()

    override val allFunctions: List<KSFunctionDeclaration>
        get() = buildList {
            if (!isShell) {
                val validCompanionObjectFunctions = companionObject?.getAllFunctions()?.filterFunctionsByOptions(clazz, options, logger) ?: emptySequence()
                addAll(validCompanionObjectFunctions)

                val validFunctions = clazz.getAllFunctions().filterFunctionsByOptions(clazz, options, logger)
                addAll(validFunctions)

                val otherFunctions = clazz.declarations.filterIsInstance<KSFunctionDeclaration>().filterNot { it in clazz.getAllFunctions() }.filterFunctionsByOptions(clazz, options, logger)
                addAll(otherFunctions)
            }

            addAll(extensionFunctions)
        }

    override fun addExtensionFunction(function: KSFunctionDeclaration) {
        extensionFunctions.add(function)
    }

    override fun addExtensionProperty(property: KSPropertyDeclaration) {
        extensionProperties.add(property)
    }

    abstract override fun build(): AbstractElement?
}