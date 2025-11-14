package uml.element

import Options
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isOpen
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import filterFunctionsByOptions
import filterPropertiesByOptions
import isValid
import uml.fullQualifiedName

abstract class DiagramElementBuilder(val clazz: KSClassDeclaration, open var isShell: Boolean, val options: Options, val logger: KSPLogger? = null) {

    fun getCompanionObjectDeclaration(
        clazz: KSClassDeclaration
    ): KSClassDeclaration? {
        return clazz
            .declarations
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.OBJECT }
            .filter { it.isCompanionObject }
            .firstOrNull()
    }

    fun getCompanionObjectProperties(
        clazz: KSClassDeclaration
    ): List<KSPropertyDeclaration> {
        return getCompanionObjectDeclaration(clazz)
            ?.getAllProperties()?.toList() ?: emptyList()
    }

    fun getCompanionObjectFunctions(
        clazz: KSClassDeclaration
    ): List<KSFunctionDeclaration> {
        return getCompanionObjectDeclaration(clazz)
            ?.getAllFunctions()?.toList() ?: emptyList()
    }

    open val fullQualifiedName: String
        get() = clazz.fullQualifiedName

    open val packageName: String
        get() = clazz.packageName.asString()

    protected val extensionProperties: MutableList<KSPropertyDeclaration> = mutableListOf()

    open val inheritedProperties: List<KSPropertyDeclaration>
        get() = clazz.getAllProperties().filter { it !in clazz.getDeclaredProperties() }.filter { options.isValid(it, logger) }.toList()

    open val overriddenProperties: List<KSPropertyDeclaration>
        get() = clazz.getAllProperties().filter { it.modifiers.contains(Modifier.OVERRIDE) }.filter { options.isValid(it, logger) }.toList()

    open val newDeclaredFinalProperties: List<KSPropertyDeclaration>
        get() = clazz.getDeclaredProperties().filter { !it.isOpen() && !it.modifiers.contains(Modifier.OVERRIDE) }.filter { options.isValid(it, logger) }.toList()

    open val newDeclaredOpenProperties: List<KSPropertyDeclaration>
        get() = clazz.getDeclaredProperties().filter { it.isOpen() && !it.modifiers.contains(Modifier.OVERRIDE) }.filter { options.isValid(it, logger) }.toList()

    open val companionProperties: List<KSPropertyDeclaration>
        get() = getCompanionObjectProperties(clazz).asSequence().filter { options.isValid(it, logger) }.toList()

    val otherProperties: List<KSPropertyDeclaration>
        get() = clazz.declarations.filterIsInstance<KSPropertyDeclaration>()
            .filter { it !in inheritedProperties }
            .filter { it !in overriddenProperties }
            .filter { it !in newDeclaredFinalProperties }
            .filter { it !in newDeclaredOpenProperties }
            .filter { it !in companionProperties }
            .filterPropertiesByOptions(clazz, options, logger)
            .toList()

    open val allProperties: List<KSPropertyDeclaration>
        get() = buildList {
            if (!isShell) {
                addAll(companionProperties)
                addAll(newDeclaredOpenProperties)
                addAll(newDeclaredFinalProperties)
                addAll(otherProperties)
                if (options.showInheritedProperties || clazz.modifiers.contains(Modifier.DATA)) {
                    addAll(overriddenProperties)
                    addAll(inheritedProperties)
                }
            }

            addAll(extensionProperties)
        }.distinct()

    protected val extensionFunctions: MutableList<KSFunctionDeclaration> = mutableListOf()


    open val inheritedFunctions: List<KSFunctionDeclaration>
        get() = clazz.getAllFunctions().filter { it !in clazz.getDeclaredFunctions() }.filter { options.isValid(it, logger) }.toList()

    open val overriddenFunctions: List<KSFunctionDeclaration>
        get() = clazz.getAllFunctions().filter { it.modifiers.contains(Modifier.OVERRIDE) }.filter { options.isValid(it, logger) }.toList()

    open val newDeclaredFinalFunctions: List<KSFunctionDeclaration>
        get() = clazz.getDeclaredFunctions().filter { !it.isOpen() && !it.modifiers.contains(Modifier.OVERRIDE) }.filter { options.isValid(it, logger) }.toList()

    open val newDeclaredOpenFunctions: List<KSFunctionDeclaration>
        get() = clazz.getDeclaredFunctions().filter { it.isOpen() && !it.modifiers.contains(Modifier.OVERRIDE) }.filter { options.isValid(it, logger) }.toList()

    open val companionFunctions: List<KSFunctionDeclaration>
        get() = getCompanionObjectFunctions(clazz).asSequence().filter { options.isValid(it, logger) }.toList()

    val otherFunctions: List<KSFunctionDeclaration>
        get() = clazz.declarations.filterIsInstance<KSFunctionDeclaration>()
            .filter { it !in inheritedFunctions }
            .filter { it !in overriddenFunctions }
            .filter { it !in newDeclaredFinalFunctions }
            .filter { it !in newDeclaredOpenFunctions }
            .filter { it !in companionFunctions }
            .filterFunctionsByOptions(clazz, options, logger)
            .toList()

    open val allFunctions: List<KSFunctionDeclaration>
        get() = buildList {
            if (!isShell) {
                addAll(companionFunctions)
                addAll(newDeclaredOpenFunctions)
                addAll(newDeclaredFinalFunctions)
                addAll(otherFunctions)
                if (options.showInheritedFunctions || clazz.modifiers.contains(Modifier.DATA)) {
                    addAll(overriddenFunctions)
                    addAll(inheritedFunctions)
                }
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