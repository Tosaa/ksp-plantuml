package uml

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import w

val KSClassDeclaration.fullQualifiedName: String
    get() = "${qualifiedName?.getQualifier() ?: packageName.asString()}.${simpleName.asString()}"

val KSClassDeclaration.className: String
    get() = fullQualifiedName.replace(packageName.asString(), "").trim('.')


/**
 * Checks if this [KSFunctionDeclaration] has the modifier [Modifier.OVERRIDE]. If that is the case, the function is inherited.
 *
 * Otherwise:
 * Checks inheritance by comparing all functions of superType classes of [declarationOwner].
 * Internally the name and the return type and the parameter names and types of the functions are compared. This calls expensive operation [com.google.devtools.ksp.symbol.KSTypeReference.resolve]
 * @return `true` if the checked [KSFunctionDeclaration] is probably inherited
 */
internal fun KSFunctionDeclaration.isInheritedFunction(declarationOwner: KSClassDeclaration, logger: KSPLogger? = null): Boolean {
    if (declarationOwner.isCompanionObject) {
        return (declarationOwner.parentDeclaration as? KSClassDeclaration)?.let {
            logger.w { "isInheritedField(): owner was companion object -> resolve base class and call isInheritedField on it: $it" }
            isInheritedFunction(it, logger)
        } ?: run {
            logger.w { "isInheritedField(): owner was companion object but base class could not be resolved" }
            false
        }
    }

    if (declarationOwner.modifiers.contains(Modifier.DATA)){
        if (this.simpleName.asString().contains(Regex("component\\d"))){
            // For Data class destruction `componentN()` functions are generated and marked as Override, therefore this special case has to be handled here
            return false
        }
    }

    if (this.modifiers.contains(Modifier.OVERRIDE)) {
        return true
    }

    val superTypesOfDeclarationOwner = declarationOwner.superTypes.toList().mapNotNull { it.resolve().declaration as? KSClassDeclaration }
        .filter { it.qualifiedName?.asString() != "kotlin.Enum" } // Ensures that all Enums have enum specific properties and functions

    val potentialMatchingDeclarations = superTypesOfDeclarationOwner.flatMap { (it.getAllFunctions() + it.declarations.filterIsInstance<KSFunctionDeclaration>()).distinct() }
        .filter { it.simpleName.getShortName() == this.simpleName.getShortName() }
    return if (potentialMatchingDeclarations.isEmpty()) {
        false
    } else {
        potentialMatchingDeclarations.any {
            when {
                it == this -> return true
                it.returnType?.resolve() != this.returnType?.resolve() -> false
                it.parameters.map { it.name?.getShortName() to it.type.resolve() } != this.parameters.map { it.name?.getShortName() to it.type.resolve() } -> false
                else -> true
            }
        }
    }
}

/**
 * Checks if this [KSPropertyDeclaration] has the modifier [Modifier.OVERRIDE]. If that is the case, the property is inherited.
 *
 * Otherwise:
 * Checks inheritance by comparing all properties of superType classes of [declarationOwner].
 * Internally the name and the return type of the properties are compared. This calls expensive operation [com.google.devtools.ksp.symbol.KSTypeReference.resolve]
 * @return `true` if the checked [KSPropertyDeclaration] is probably inherited
 */
internal fun KSPropertyDeclaration.isInheritedProperty(declarationOwner: KSClassDeclaration, logger: KSPLogger? = null): Boolean {
    if (declarationOwner.isCompanionObject) {
        return (declarationOwner.parentDeclaration as? KSClassDeclaration)?.let {
            logger.w { "isInheritedField(): owner was companion object -> resolve base class and call isInheritedField on it: $it" }
            isInheritedProperty(it, logger)
        } ?: run {
            logger.w { "isInheritedField(): owner was companion object but base class could not be resolved" }
            false
        }
    }

    if (this.modifiers.contains(Modifier.OVERRIDE)) {
        return true
    }

    val superTypesOfDeclarationOwner = declarationOwner.superTypes.toList().mapNotNull { it.resolve().declaration as? KSClassDeclaration }
        .filter { it.qualifiedName?.asString() != "kotlin.Enum" } // Ensures that all Enums have enum specific properties and functions

    val potentialMatchingDeclarations = superTypesOfDeclarationOwner.flatMap { (it.getAllProperties() + it.declarations.filterIsInstance<KSPropertyDeclaration>()).distinct() }
        .filter { it.simpleName.getShortName() == this.simpleName.getShortName() }
    return if (potentialMatchingDeclarations.isEmpty()) {
        false
    } else {
        potentialMatchingDeclarations.any {
            when {
                it == this -> true
                it.type.resolve() != this.type.resolve() -> false
                else -> true
            }
        }
    }
}
