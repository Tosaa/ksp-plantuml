package uml.element

import Options
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import filterFunctionsByOptions
import filterPropertiesByOptions
import uml.fullQualifiedName
import w

/**
 * Everything that is rendered is resolved when the builder is created or when a member is added to it.
 * The symbols of a processing round must not be accessed in a later round or when the diagram is written,
 * which happens as soon as another processor generates code and causes a further round.
 */
abstract class DiagramElementBuilder(val clazz: KSClassDeclaration, isShellInitially: Boolean, val options: Options, val logger: KSPLogger? = null) {

    val companionObject = clazz.declarations.filter { it is KSClassDeclaration && it.isCompanionObject }.map { it as? KSClassDeclaration }.firstOrNull()

    open val fullQualifiedName: String = clazz.fullQualifiedName

    open val packageName: String = clazz.packageName.asString()

    /**
     * The qualified names of the classes and interfaces that [clazz] extends or implements, without the ones of the kotlin and java standard library.
     */
    val superTypeNames: List<String> = runCatching {
        clazz.superTypes
            .mapNotNull { it.resolve().declaration as? KSClassDeclaration }
            .filterNot { it.packageName.asString().startsWith("kotlin") }
            .filterNot { it.packageName.asString().startsWith("java") }
            .mapNotNull { it.qualifiedName?.asString() }
            .toList()
    }.getOrElse {
        logger.w { "Could not resolve the supertypes of ${clazz.fullQualifiedName}: ${it.message}" }
        emptyList()
    }

    /**
     * A shell renders only the members that were added explicitly, e.g. extension functions.
     */
    open val isShell: Boolean = isShellInitially

    var allProperties: List<Field> = if (isShellInitially) emptyList() else computeMemberFields()
        private set

    var allFunctions: List<Method> = if (isShellInitially) emptyList() else computeMemberMethods()
        private set

    fun addExtensionFunction(function: KSFunctionDeclaration) {
        allFunctions = allFunctions + function.toMethod(options)
    }

    fun addExtensionProperty(property: KSPropertyDeclaration) {
        allProperties = allProperties + property.toField(options)
    }

    /**
     * Takes over the members that were added to the [shell] of the same class.
     * A shell is replaced by a builder of the declaration that is processed, since the shell might belong to a declaration of an earlier processing round.
     */
    fun addMembersOf(shell: DiagramElementBuilder) {
        allProperties = allProperties + shell.allProperties
        allFunctions = allFunctions + shell.allFunctions
    }

    abstract fun build(): DiagramElement?

    private fun computeMemberFields(): List<Field> = buildList {
        val validCompanionObjectProperties = companionObject?.getAllProperties()
            ?.filterPropertiesByOptions(clazz, options, logger) ?: emptySequence()
        addAll(validCompanionObjectProperties.map { it.toField(options) })
        val declaredAndInheritedProperties = clazz.getAllProperties().toList()
        addAll(
            declaredAndInheritedProperties.asSequence()
                .filterPropertiesByOptions(clazz, options, logger)
                .map { it.toField(options) }
        )
        val declaredAndInherited = declaredAndInheritedProperties.toSet()
        val otherProperties = clazz.declarations
            .filterIsInstance<KSPropertyDeclaration>()
            .filterNot { it in declaredAndInherited }
            .filterPropertiesByOptions(clazz, options, logger)
        addAll(otherProperties.map { it.toField(options) })
    }

    private fun computeMemberMethods(): List<Method> = buildList {
        val validCompanionObjectFunctions = companionObject?.getAllFunctions()
            ?.filterFunctionsByOptions(clazz, options, logger) ?: emptySequence()
        addAll(validCompanionObjectFunctions.map { it.toMethod(options) })
        val declaredAndInheritedFunctions = clazz.getAllFunctions().toList()
        addAll(
            declaredAndInheritedFunctions.asSequence()
                .filterFunctionsByOptions(clazz, options, logger)
                .map { it.toMethod(options) }
        )
        val declaredAndInherited = declaredAndInheritedFunctions.toSet()
        val otherFunctions = clazz.declarations
            .filterIsInstance<KSFunctionDeclaration>()
            .filterNot { it in declaredAndInherited }
            .filterFunctionsByOptions(clazz, options, logger)
        addAll(otherFunctions.map { it.toMethod(options) })
    }
}
