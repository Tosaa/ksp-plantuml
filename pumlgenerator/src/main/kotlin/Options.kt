import OptionConstants.Companion.MAX_RELATIONS
import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import uml.isInheritedFunction
import uml.isInheritedProperty

data class Options(
    val includedPackages: List<String> = emptyList(),
    val excludedPackages: List<String> = emptyList(),
    val excludedClassNames: List<String> = emptyList(),
    val excludedPropertyNames: List<String> = emptyList(),
    val excludedFunctionNames: List<String> = DEFAULT_EXCLUDED_FUNCTIONS,
    val showInheritedProperties: Boolean = false,
    val showInheritedFunctions: Boolean = false,
    val showVisibilityModifiers: Boolean = true,
    val showExtensions: Boolean = true,
    val markExtensions: Boolean = true,
    val showPublicClasses: Boolean = true,
    val showPublicProperties: Boolean = true,
    val showPublicFunctions: Boolean = true,
    val showInternalClasses: Boolean = true,
    val showInternalProperties: Boolean = true,
    val showInternalFunctions: Boolean = true,
    val showPrivateClasses: Boolean = true,
    val showPrivateProperties: Boolean = true,
    val showPrivateFunctions: Boolean = true,
    val showInheritance: Boolean = true,
    val showPropertyRelations: Boolean = true,
    val showFunctionRelations: Boolean = true,
    val showIndirectRelations: Boolean = true,
    val maxRelations: Int = MAX_RELATIONS,
    val showPackages: Boolean = false,
    val allowEmptyPackage: Boolean = true,
    val prefix: String = "",
    val postfix: String = "",
    val title: String = "",
) {
    constructor(kspProcessorOptions: Map<String, String>) : this(
        includedPackages = kspProcessorOptions[OptionConstants.KEY_INCLUDED_PACKAGES.identifier]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList(),
        excludedPackages = kspProcessorOptions[OptionConstants.KEY_EXCLUDE_PACKAGES.identifier]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList(),
        excludedClassNames = kspProcessorOptions[OptionConstants.KEY_EXCLUDE_CLASS_NAMES.identifier]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList(),
        excludedPropertyNames = kspProcessorOptions[OptionConstants.KEY_EXCLUDE_PROPERTY_NAMES.identifier]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList(),
        excludedFunctionNames = kspProcessorOptions[OptionConstants.KEY_EXCLUDE_FUNCTION_NAMES.identifier]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: DEFAULT_EXCLUDED_FUNCTIONS,
        showVisibilityModifiers = kspProcessorOptions[OptionConstants.KEY_SHOW_VISIBILITY_MODIFIERS.identifier]?.equals("true", true) ?: true,
        markExtensions = kspProcessorOptions[OptionConstants.KEY_MARK_EXTENSIONS.identifier]?.equals("true", true) ?: true,
        showInheritedProperties = kspProcessorOptions[OptionConstants.KEY_SHOW_INHERITED_PROPERTIES.identifier]?.equals("true", true) ?: false,
        showInheritedFunctions = kspProcessorOptions[OptionConstants.KEY_SHOW_INHERITED_FUNCTIONS.identifier]?.equals("true", true) ?: false,
        showExtensions = kspProcessorOptions[OptionConstants.KEY_SHOW_EXTENSIONS.identifier]?.equals("true", true) ?: true,
        showPublicClasses = kspProcessorOptions[OptionConstants.KEY_SHOW_PUBLIC_CLASSES.identifier]?.equals("true", true) ?: true,
        showPublicProperties = kspProcessorOptions[OptionConstants.KEY_SHOW_PUBLIC_PROPERTIES.identifier]?.equals("true", true) ?: true,
        showPublicFunctions = kspProcessorOptions[OptionConstants.KEY_SHOW_PUBLIC_FUNCTIONS.identifier]?.equals("true", true) ?: true,
        showInternalClasses = kspProcessorOptions[OptionConstants.KEY_SHOW_INTERNAL_CLASSES.identifier]?.equals("true", true) ?: true,
        showInternalProperties = kspProcessorOptions[OptionConstants.KEY_SHOW_INTERNAL_PROPERTIES.identifier]?.equals("true", true) ?: true,
        showInternalFunctions = kspProcessorOptions[OptionConstants.KEY_SHOW_INTERNAL_FUNCTIONS.identifier]?.equals("true", true) ?: true,
        showPrivateClasses = kspProcessorOptions[OptionConstants.KEY_SHOW_PRIVATE_CLASSES.identifier]?.equals("true", true) ?: true,
        showPrivateProperties = kspProcessorOptions[OptionConstants.KEY_SHOW_PRIVATE_PROPERTIES.identifier]?.equals("true", true) ?: true,
        showPrivateFunctions = kspProcessorOptions[OptionConstants.KEY_SHOW_PRIVATE_FUNCTIONS.identifier]?.equals("true", true) ?: true,
        showInheritance = kspProcessorOptions[OptionConstants.KEY_SHOW_INHERITANCE.identifier]?.equals("true", true) ?: true,
        showPropertyRelations = kspProcessorOptions[OptionConstants.KEY_SHOW_PROPERTY_RELATIONS.identifier]?.equals("true", true) ?: true,
        showFunctionRelations = kspProcessorOptions[OptionConstants.KEY_SHOW_FUNCTION_RELATIONS.identifier]?.equals("true", true) ?: true,
        showIndirectRelations = kspProcessorOptions[OptionConstants.KEY_SHOW_INDIRECT_RELATIONS.identifier]?.equals("true", true) ?: true,
        maxRelations = kspProcessorOptions[OptionConstants.KEY_MAX_RELATIONS.identifier]?.toIntOrNull() ?: MAX_RELATIONS,
        showPackages = kspProcessorOptions[OptionConstants.KEY_SHOW_PACKAGES.identifier]?.equals("true", true) ?: false,
        allowEmptyPackage = kspProcessorOptions[OptionConstants.KEY_ALLOW_EMPTY_PACKAGE.identifier]?.equals("true", true) ?: false,
        prefix = kspProcessorOptions[OptionConstants.KEY_PREFIX.identifier] ?: "",
        postfix = kspProcessorOptions[OptionConstants.KEY_POSTFIX.identifier] ?: "",
        title = kspProcessorOptions[OptionConstants.KEY_TITLE.identifier] ?: "",
    )

    fun asMap(): Map<String, String> {
        return mutableMapOf<String, String>().apply {
            put(OptionConstants.KEY_INCLUDED_PACKAGES.identifier, includedPackages.joinToString(", "))
            put(OptionConstants.KEY_EXCLUDE_PACKAGES.identifier, excludedPackages.joinToString(", "))
            put(OptionConstants.KEY_EXCLUDE_CLASS_NAMES.identifier, excludedClassNames.joinToString(", "))
            put(OptionConstants.KEY_EXCLUDE_PROPERTY_NAMES.identifier, excludedPropertyNames.joinToString(", "))
            put(OptionConstants.KEY_EXCLUDE_FUNCTION_NAMES.identifier, excludedFunctionNames.joinToString(", "))
            put(OptionConstants.KEY_SHOW_VISIBILITY_MODIFIERS.identifier, if (showVisibilityModifiers) "true" else "false")
            put(OptionConstants.KEY_SHOW_INHERITED_PROPERTIES.identifier, if (showInheritedProperties) "true" else "false")
            put(OptionConstants.KEY_SHOW_INHERITED_FUNCTIONS.identifier, if (showInheritedFunctions) "true" else "false")
            put(OptionConstants.KEY_MARK_EXTENSIONS.identifier, if (markExtensions) "true" else "false")
            put(OptionConstants.KEY_SHOW_EXTENSIONS.identifier, if (showExtensions) "true" else "false")
            put(OptionConstants.KEY_SHOW_PUBLIC_CLASSES.identifier, if (showPublicClasses) "true" else "false")
            put(OptionConstants.KEY_SHOW_PUBLIC_PROPERTIES.identifier, if (showPublicProperties) "true" else "false")
            put(OptionConstants.KEY_SHOW_PUBLIC_FUNCTIONS.identifier, if (showPublicFunctions) "true" else "false")
            put(OptionConstants.KEY_SHOW_INTERNAL_CLASSES.identifier, if (showInternalClasses) "true" else "false")
            put(OptionConstants.KEY_SHOW_INTERNAL_PROPERTIES.identifier, if (showInternalProperties) "true" else "false")
            put(OptionConstants.KEY_SHOW_INTERNAL_FUNCTIONS.identifier, if (showInternalFunctions) "true" else "false")
            put(OptionConstants.KEY_SHOW_PRIVATE_CLASSES.identifier, if (showPrivateClasses) "true" else "false")
            put(OptionConstants.KEY_SHOW_PRIVATE_PROPERTIES.identifier, if (showPrivateProperties) "true" else "false")
            put(OptionConstants.KEY_SHOW_PRIVATE_FUNCTIONS.identifier, if (showPrivateFunctions) "true" else "false")
            put(OptionConstants.KEY_SHOW_INHERITANCE.identifier, if (showInheritance) "true" else "false")
            put(OptionConstants.KEY_SHOW_PROPERTY_RELATIONS.identifier, if (showPropertyRelations) "true" else "false")
            put(OptionConstants.KEY_SHOW_FUNCTION_RELATIONS.identifier, if (showFunctionRelations) "true" else "false")
            put(OptionConstants.KEY_SHOW_INDIRECT_RELATIONS.identifier, if (showIndirectRelations) "true" else "false")
            put(OptionConstants.KEY_MAX_RELATIONS.identifier, maxRelations.toString())
            put(OptionConstants.KEY_SHOW_PACKAGES.identifier, if (showPackages) "true" else "false")
            put(OptionConstants.KEY_ALLOW_EMPTY_PACKAGE.identifier, if (allowEmptyPackage) "true" else "false")
            put(OptionConstants.KEY_PREFIX.identifier, prefix)
            put(OptionConstants.KEY_POSTFIX.identifier, postfix)
            put(OptionConstants.KEY_TITLE.identifier, title)
        }
    }

    companion object {
        val DEFAULT_EXCLUDED_FUNCTIONS = listOf("<init>", "toString", "equals", "hashCode")
    }
}

fun Options?.isValid(packageName: String, logger: KSPLogger? = null): Boolean = runCatching {
    when {
        this == null ->
            true

        packageName.equals("kotlin.collections") -> true

        packageName.isBlank() && !this.allowEmptyPackage -> {
            logger.v { "Exclude '$packageName' since empty Package deactivated by the options" }
            false
        }

        packageName.isBlank() && this.allowEmptyPackage -> true

        packageName.isNotBlank() && excludedPackages.isEmpty() && includedPackages.isEmpty() ->
            true

        this.excludedPackages.any { it == packageName } -> {
            logger.v { "Package is excluded: '$packageName'" }
            false
        }


        this.excludedPackages.any { (packageName.startsWith(it)) } -> {
            val packageThatExcludesThisClass = this.excludedPackages.find { (packageName.startsWith(it)) }
            logger.v { "Exclude package '$packageName' by excluded package: $packageThatExcludesThisClass" }
            false
        }

        this.includedPackages.isNotEmpty() && this.includedPackages.none { it == packageName || packageName.startsWith(it) } -> {
            logger.v { "Exclude package '$packageName' since it does not match the included packages: ${this.includedPackages.joinToString()}" }
            false
        }

        else -> true
    }
}.getOrElse { throwable ->
    logger.w { "Exclude package '$packageName' due to internal Kotlin error:\n${throwable.stackTraceToString()}" }
    false
}

fun Options?.isValid(type: KSType?, logger: KSPLogger? = null): Boolean = runCatching {
    when {
        this == null ->
            true

        type == null ->
            true


        type.declaration.isPublic() && !this.showPublicClasses -> {
            logger.v { "Exclude '${type.declaration.simpleName.getShortName()}' since its Public and Public deactivated by the options" }
            false
        }

        type.declaration.isInternal() && !this.showInternalClasses -> {
            logger.v { "Exclude '${type.declaration.simpleName.getShortName()}' since its Internal and Internal deactivated by the options" }
            false
        }

        type.declaration.isPrivate() && !this.showPrivateClasses -> {
            logger.v { "Exclude '${type.declaration.simpleName.getShortName()}' since its Private and Private deactivated by the options" }
            false
        }

        !isValid(type.declaration.packageName.asString(), logger) -> false

        else -> true
    }
}.getOrElse { throwable ->
    logger.w { "Exclude '${type?.declaration?.simpleName?.asString()}' due to internal Kotlin error:\n${throwable.stackTraceToString()}" }
    false
}

fun Options?.isValid(declaration: KSClassDeclaration, logger: KSPLogger? = null): Boolean = runCatching {
    when {
        this == null ->
            true

        declaration.isPublic() && !this.showPublicClasses -> {
            logger.v { "Exclude '${declaration.simpleName.getShortName()}' since its Public and Public deactivated by the options" }
            false
        }

        declaration.isInternal() && !this.showInternalClasses -> {
            logger.v { "Exclude '${declaration.simpleName.getShortName()}' since its Internal and Internal deactivated by the options" }
            false
        }

        declaration.isPrivate() && !this.showPrivateClasses -> {
            logger.v { "Exclude '${declaration.simpleName.getShortName()}' since its Private and Private deactivated by the options" }
            false
        }

        declaration.simpleName.asString() in this.excludedClassNames -> {
            logger.v { "Exclude '${declaration.simpleName.asString()}' since its in excludedClassNames" }
            false
        }

        !isValid(declaration.packageName.asString(), logger) -> false

        else -> true
    }
}.getOrElse { throwable ->
    logger.w { "Exclude '${declaration.simpleName.asString()}' due to internal Kotlin error:\n${throwable.stackTraceToString()}" }
    false
}

fun Options?.isValid(declaration: KSTypeAlias, logger: KSPLogger? = null): Boolean = runCatching {
    when {
        this == null ->
            true

        declaration.isPublic() && !this.showPublicClasses -> {
            logger.v { "Exclude '${declaration.simpleName.getShortName()}' since its Public and Public deactivated by the options" }
            false
        }

        declaration.isInternal() && !this.showInternalClasses -> {
            logger.v { "Exclude '${declaration.simpleName.getShortName()}' since its Internal and Internal deactivated by the options" }
            false
        }

        declaration.isPrivate() && !this.showPrivateClasses -> {
            logger.v { "Exclude '${declaration.simpleName.getShortName()}' since its Private and Private deactivated by the options" }
            false
        }

        declaration.simpleName.asString() in this.excludedClassNames -> {
            logger.v { "Exclude '${declaration.simpleName.asString()}' since its in excludedClassNames" }
            false
        }

        !isValid(declaration.packageName.asString(), logger) -> false

        else -> true
    }
}.getOrElse { throwable ->
    logger.w { "Exclude '${declaration.simpleName.asString()}' due to internal Kotlin error:\n${throwable.stackTraceToString()}" }
    false
}

fun Options?.isValid(declaration: KSPropertyDeclaration, logger: KSPLogger? = null): Boolean = runCatching {
    when {
        this == null ->
            true

        declaration.isPublic() && !this.showPublicProperties -> {
            logger.v { "Exclude '${declaration.simpleName.getShortName()}' since its Public and Public deactivated by the options" }
            false
        }

        declaration.isInternal() && !this.showInternalProperties -> {
            logger.v { "Exclude '${declaration.simpleName.getShortName()}' since its Internal and Internal deactivated by the options" }
            false
        }

        declaration.isPrivate() && !this.showPrivateProperties -> {
            logger.v { "Exclude '${declaration.simpleName.getShortName()}' since its Private and Private deactivated by the options" }
            false
        }

        declaration.simpleName.asString() in this.excludedPropertyNames -> {
            logger.v { "Exclude '${declaration.simpleName.asString()}' since its in excludedPropertyNames" }
            false
        }

        declaration.extensionReceiver != null && !this.showExtensions -> {
            logger.v { "Exclude '${declaration.simpleName.asString()}' since Extension Functions are deactivated by the options" }
            false
        }

        else -> true
    }
}.getOrElse { throwable ->
    logger.w { "Exclude '${declaration.simpleName.asString()}' due to internal Kotlin error:\n${throwable.stackTraceToString()}" }
    false
}

fun Options?.isValid(declaration: KSFunctionDeclaration, logger: KSPLogger? = null): Boolean = runCatching {
    when {
        this == null ->
            true

        declaration.isPublic() && !this.showPublicFunctions -> {
            logger.v { "Exclude '${declaration.simpleName.getShortName()}' since its Public and Public deactivated by the options" }
            false
        }

        declaration.isInternal() && !this.showInternalFunctions -> {
            logger.v { "Exclude '${declaration.simpleName.getShortName()}' since its Internal and Internal deactivated by the options" }
            false
        }

        declaration.isPrivate() && !this.showPrivateFunctions -> {
            logger.v { "Exclude '${declaration.simpleName.getShortName()}' since its Private and Private deactivated by the options" }
            false
        }

        declaration.simpleName.asString() in this.excludedFunctionNames -> {
            logger.v { "Exclude '${declaration.simpleName.asString()}' since its in excludedFunctionNames" }
            false
        }

        declaration.extensionReceiver != null && !this.showExtensions -> {
            logger.v { "Exclude '${declaration.simpleName.asString()}' since Extension Functions are deactivated by the options" }
            false
        }

        else -> true
    }
}.getOrElse { throwable ->
    logger.w { "Exclude '${declaration.simpleName.asString()}' due to internal Kotlin error:\n${throwable.stackTraceToString()}" }
    false
}

fun Options?.isValid(declaration: KSDeclaration, logger: KSPLogger? = null): Boolean = when (declaration) {
    is KSPropertyDeclaration -> isValid(declaration, logger)
    is KSFunctionDeclaration -> isValid(declaration, logger)
    is KSClassDeclaration -> isValid(declaration, logger)
    is KSTypeAlias -> isValid(declaration, logger)
    else -> {
        logger.w { "No validation function implemented for $declaration -> return True" }
        true
    }
}

fun Sequence<KSPropertyDeclaration>.filterPropertiesByOptions(clazz: KSClassDeclaration, options: Options, logger: KSPLogger?): Sequence<KSPropertyDeclaration> {
    return if (!options.showInheritedProperties) {
        this.filter { options.isValid(it, logger) }
            .filter { property ->
                !property.isInheritedProperty(clazz, logger).also {
                    if (it) {
                        logger.v { "filterPropertiesByOptions(): Filtered inherited property ${clazz.simpleName.asString()}.${property.simpleName.asString()}" }
                    }
                }
            }
    } else {
        this.filter { options.isValid(it, logger) }
    }
}

fun Sequence<KSFunctionDeclaration>.filterFunctionsByOptions(clazz: KSClassDeclaration, options: Options, logger: KSPLogger?): Sequence<KSFunctionDeclaration> {
    return if (!options.showInheritedFunctions) {
        this.filter { options.isValid(it, logger) }
            .filter { function ->
                !function.isInheritedFunction(clazz, logger).also {
                    if (it) {
                        logger.v { "filterFunctionsByOptions(): Filtered inherited function ${clazz.simpleName.asString()}.${function.simpleName.asString()}" }
                    }
                }
            }
    } else {
        this.filter { options.isValid(it, logger) }
    }
}