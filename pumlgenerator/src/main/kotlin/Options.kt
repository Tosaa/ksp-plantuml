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
import uml.isGeneratedMemberOf
import uml.isInheritedFunction
import uml.isInheritedProperty
import uml.isJvmRootFunction

data class Options(
    val includedPackages: List<String> = emptyList(),
    val excludedPackages: List<String> = emptyList(),
    val excludedClassNames: List<String> = emptyList(),
    val excludedClassNamePrefixes: List<String> = emptyList(),
    val excludedClassNameSuffixes: List<String> = emptyList(),
    val excludedClassNameContains: List<String> = emptyList(),
    val filterPreset: String = DEFAULT_FILTER_PRESET,
    val excludedPropertyNames: List<String> = emptyList(),
    val excludedFunctionNames: List<String> = DEFAULT_EXCLUDED_FUNCTIONS,
    val showInheritedProperties: Boolean = false,
    val showInheritedFunctions: Boolean = false,
    val showJavaObjectMembers: Boolean = false,
    val showGeneratedMembers: Boolean = false,
    val notation: Notation = Notation.DEFAULT,
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
    val outputFileName: String = "",
    val saveLogToFile: Boolean = true
) {
    constructor(kspProcessorOptions: Map<String, String>) : this(
        includedPackages = kspProcessorOptions[OptionConstants.KEY_INCLUDED_PACKAGES.identifier]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList(),
        excludedPackages = kspProcessorOptions[OptionConstants.KEY_EXCLUDE_PACKAGES.identifier]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList(),
        excludedClassNames = kspProcessorOptions[OptionConstants.KEY_EXCLUDE_CLASS_NAMES.identifier]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList(),
        excludedClassNamePrefixes = kspProcessorOptions[OptionConstants.KEY_EXCLUDE_CLASS_NAME_PREFIXES.identifier]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList(),
        excludedClassNameSuffixes = kspProcessorOptions[OptionConstants.KEY_EXCLUDE_CLASS_NAME_SUFFIXES.identifier]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList(),
        excludedClassNameContains = kspProcessorOptions[OptionConstants.KEY_EXCLUDE_CLASS_NAME_CONTAINS.identifier]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList(),
        filterPreset = kspProcessorOptions[OptionConstants.KEY_FILTER_PRESET.identifier]?.trim()?.takeIf { it.isNotEmpty() } ?: DEFAULT_FILTER_PRESET,
        excludedPropertyNames = kspProcessorOptions[OptionConstants.KEY_EXCLUDE_PROPERTY_NAMES.identifier]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList(),
        excludedFunctionNames = kspProcessorOptions[OptionConstants.KEY_EXCLUDE_FUNCTION_NAMES.identifier]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: DEFAULT_EXCLUDED_FUNCTIONS,
        notation = kspProcessorOptions[OptionConstants.KEY_NOTATION.identifier]?.let { Notation.fromName(it) } ?: Notation.DEFAULT,
        showVisibilityModifiers = kspProcessorOptions[OptionConstants.KEY_SHOW_VISIBILITY_MODIFIERS.identifier]?.equals("true", true) ?: true,
        markExtensions = kspProcessorOptions[OptionConstants.KEY_MARK_EXTENSIONS.identifier]?.equals("true", true) ?: true,
        showInheritedProperties = kspProcessorOptions[OptionConstants.KEY_SHOW_INHERITED_PROPERTIES.identifier]?.equals("true", true) ?: false,
        showInheritedFunctions = kspProcessorOptions[OptionConstants.KEY_SHOW_INHERITED_FUNCTIONS.identifier]?.equals("true", true) ?: false,
        showJavaObjectMembers = kspProcessorOptions[OptionConstants.KEY_SHOW_JAVA_OBJECT_MEMBERS.identifier]?.equals("true", true) ?: false,
        showGeneratedMembers = kspProcessorOptions[OptionConstants.KEY_SHOW_GENERATED_MEMBERS.identifier]?.equals("true", true) ?: false,
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
        outputFileName = kspProcessorOptions[OptionConstants.KEY_OUTPUT_FILE_PATH.identifier] ?: "",
        saveLogToFile = kspProcessorOptions[OptionConstants.KEY_SAVE_LOG_TO_FILE.identifier]?.equals("true", true) ?: true,
    )

    /**
     * The class names that are excluded by the [filterPreset] together with the ones that are excluded explicitly.
     * It is created together with the options, so that an unknown [filterPreset] is reported before any class is processed.
     */
    val classNameFilter: ClassNameFilter = ClassNameFilter.fromPresetName(filterPreset) + ClassNameFilter(
        excludedExact = excludedClassNames,
        excludedPrefixes = excludedClassNamePrefixes,
        excludedSuffixes = excludedClassNameSuffixes,
        excludedContains = excludedClassNameContains,
    )

    fun asMap(): Map<String, String> {
        return mutableMapOf<String, String>().apply {
            put(OptionConstants.KEY_INCLUDED_PACKAGES.identifier, includedPackages.joinToString(", "))
            put(OptionConstants.KEY_EXCLUDE_PACKAGES.identifier, excludedPackages.joinToString(", "))
            put(OptionConstants.KEY_EXCLUDE_CLASS_NAMES.identifier, excludedClassNames.joinToString(", "))
            put(OptionConstants.KEY_EXCLUDE_CLASS_NAME_PREFIXES.identifier, excludedClassNamePrefixes.joinToString(", "))
            put(OptionConstants.KEY_EXCLUDE_CLASS_NAME_SUFFIXES.identifier, excludedClassNameSuffixes.joinToString(", "))
            put(OptionConstants.KEY_EXCLUDE_CLASS_NAME_CONTAINS.identifier, excludedClassNameContains.joinToString(", "))
            put(OptionConstants.KEY_FILTER_PRESET.identifier, filterPreset)
            put(OptionConstants.KEY_EXCLUDE_PROPERTY_NAMES.identifier, excludedPropertyNames.joinToString(", "))
            put(OptionConstants.KEY_EXCLUDE_FUNCTION_NAMES.identifier, excludedFunctionNames.joinToString(", "))
            put(OptionConstants.KEY_NOTATION.identifier, notation.name)
            put(OptionConstants.KEY_SHOW_VISIBILITY_MODIFIERS.identifier, showVisibilityModifiers.toString())
            put(OptionConstants.KEY_SHOW_INHERITED_PROPERTIES.identifier, showInheritedProperties.toString())
            put(OptionConstants.KEY_SHOW_INHERITED_FUNCTIONS.identifier, showInheritedFunctions.toString())
            put(OptionConstants.KEY_SHOW_JAVA_OBJECT_MEMBERS.identifier, showJavaObjectMembers.toString())
            put(OptionConstants.KEY_SHOW_GENERATED_MEMBERS.identifier, showGeneratedMembers.toString())
            put(OptionConstants.KEY_MARK_EXTENSIONS.identifier, markExtensions.toString())
            put(OptionConstants.KEY_SHOW_EXTENSIONS.identifier, showExtensions.toString())
            put(OptionConstants.KEY_SHOW_PUBLIC_CLASSES.identifier, showPublicClasses.toString())
            put(OptionConstants.KEY_SHOW_PUBLIC_PROPERTIES.identifier, showPublicProperties.toString())
            put(OptionConstants.KEY_SHOW_PUBLIC_FUNCTIONS.identifier, showPublicFunctions.toString())
            put(OptionConstants.KEY_SHOW_INTERNAL_CLASSES.identifier, showInternalClasses.toString())
            put(OptionConstants.KEY_SHOW_INTERNAL_PROPERTIES.identifier, showInternalProperties.toString())
            put(OptionConstants.KEY_SHOW_INTERNAL_FUNCTIONS.identifier, showInternalFunctions.toString())
            put(OptionConstants.KEY_SHOW_PRIVATE_CLASSES.identifier, showPrivateClasses.toString())
            put(OptionConstants.KEY_SHOW_PRIVATE_PROPERTIES.identifier, showPrivateProperties.toString())
            put(OptionConstants.KEY_SHOW_PRIVATE_FUNCTIONS.identifier, showPrivateFunctions.toString())
            put(OptionConstants.KEY_SHOW_INHERITANCE.identifier, showInheritance.toString())
            put(OptionConstants.KEY_SHOW_PROPERTY_RELATIONS.identifier, showPropertyRelations.toString())
            put(OptionConstants.KEY_SHOW_FUNCTION_RELATIONS.identifier, showFunctionRelations.toString())
            put(OptionConstants.KEY_SHOW_INDIRECT_RELATIONS.identifier, showIndirectRelations.toString())
            put(OptionConstants.KEY_MAX_RELATIONS.identifier, maxRelations.toString())
            put(OptionConstants.KEY_SHOW_PACKAGES.identifier, showPackages.toString())
            put(OptionConstants.KEY_ALLOW_EMPTY_PACKAGE.identifier, allowEmptyPackage.toString())
            put(OptionConstants.KEY_PREFIX.identifier, prefix)
            put(OptionConstants.KEY_POSTFIX.identifier, postfix)
            put(OptionConstants.KEY_TITLE.identifier, title)
            put(OptionConstants.KEY_OUTPUT_FILE_PATH.identifier, outputFileName)
            put(OptionConstants.KEY_SAVE_LOG_TO_FILE.identifier, saveLogToFile.toString())
        }
    }

    companion object {
        val DEFAULT_EXCLUDED_FUNCTIONS = listOf("<init>", "toString", "equals", "hashCode")
        const val DEFAULT_FILTER_PRESET = "NONE"
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
    // The type is not accessed again, since it might be the reason of the error
    logger.w { "Exclude type due to internal Kotlin error:\n${throwable.stackTraceToString()}" }
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

        !this.classNameFilter.shouldInclude(declaration.simpleName.asString()) -> {
            logger.v { "Exclude '${declaration.simpleName.asString()}' since its name is excluded by excludedClassNames, a prefix, a suffix, a fragment or the filterPreset" }
            false
        }

        !isValid(declaration.packageName.asString(), logger) -> false

        else -> true
    }
}.getOrElse { throwable ->
    // The declaration is not accessed again, since it might be the reason of the error
    logger.w { "Exclude declaration due to internal Kotlin error:\n${throwable.stackTraceToString()}" }
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

        !this.classNameFilter.shouldInclude(declaration.simpleName.asString()) -> {
            logger.v { "Exclude '${declaration.simpleName.asString()}' since its name is excluded by excludedClassNames, a prefix, a suffix, a fragment or the filterPreset" }
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
    // The filters by name are applied first, since the check for inheritance has to resolve types
    val validProperties = this.filter { options.isValid(it, logger) }
        .filter { property ->
            options.showGeneratedMembers || !property.isGeneratedMemberOf(clazz).also {
                if (it) {
                    logger.v { "filterPropertiesByOptions(): Filtered generated property ${clazz.simpleName.asString()}.${property.simpleName.asString()}" }
                }
            }
        }
    return if (!options.showInheritedProperties) {
        validProperties
            .filter { property ->
                !property.isInheritedProperty(clazz, logger).also {
                    if (it) {
                        logger.v { "filterPropertiesByOptions(): Filtered inherited property ${clazz.simpleName.asString()}.${property.simpleName.asString()}" }
                    }
                }
            }
    } else {
        validProperties
    }
}

fun Sequence<KSFunctionDeclaration>.filterFunctionsByOptions(clazz: KSClassDeclaration, options: Options, logger: KSPLogger?): Sequence<KSFunctionDeclaration> {
    // The filters by name are applied first, since the check for inheritance has to resolve types
    val validFunctions = this.filter { options.isValid(it, logger) }
        .filter { function ->
            options.showJavaObjectMembers || !function.isJvmRootFunction().also {
                if (it) {
                    logger.v { "filterFunctionsByOptions(): Filtered function of java.lang.Object ${clazz.simpleName.asString()}.${function.simpleName.asString()}" }
                }
            }
        }
        .filter { function ->
            options.showGeneratedMembers || !function.isGeneratedMemberOf(clazz).also {
                if (it) {
                    logger.v { "filterFunctionsByOptions(): Filtered generated function ${clazz.simpleName.asString()}.${function.simpleName.asString()}" }
                }
            }
        }
    return if (!options.showInheritedFunctions) {
        validFunctions
            .filter { function ->
                !function.isInheritedFunction(clazz, logger).also {
                    if (it) {
                        logger.v { "filterFunctionsByOptions(): Filtered inherited function ${clazz.simpleName.asString()}.${function.simpleName.asString()}" }
                    }
                }
            }
    } else {
        validFunctions
    }
}