package uml.element

import Options
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import filterFunctionsByOptions
import filterPropertiesByOptions
import uml.fullQualifiedName

abstract class DiagramElementBuilder(val clazz: KSClassDeclaration, open var isShell: Boolean, val options: Options, val logger: KSPLogger? = null) {

    val companionObject = clazz.declarations.filter { it is KSClassDeclaration && it.isCompanionObject }.map { it as? KSClassDeclaration }.firstOrNull()

    open val fullQualifiedName: String
        get() = clazz.fullQualifiedName

    open val packageName: String
        get() = clazz.packageName.asString()

    protected val extensionProperties: MutableList<KSPropertyDeclaration> = mutableListOf()

    open val allProperties: List<KSPropertyDeclaration>
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

    open val allFunctions: List<KSFunctionDeclaration>
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

    fun addExtensionFunction(function: KSFunctionDeclaration) {
        extensionFunctions.add(function)
    }

    fun addExtensionProperty(property: KSPropertyDeclaration) {
        extensionProperties.add(property)
    }

    abstract fun build(): DiagramElement?
}