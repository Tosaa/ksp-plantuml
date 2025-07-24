import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import uml.isInheritedField
import uml.isInheritedFunction
import uml.isInheritedProperty

// Active includes/excludes
private const val KEY_INCLUDED_PACKAGES = "puml.includedPackages"
private const val KEY_EXCLUDE_PACKAGES = "puml.excludedPackages"
private const val KEY_EXCLUDE_CLASS_NAMES = "puml.excludedClassNames"
private const val KEY_EXCLUDE_PROPERTY_NAMES = "puml.excludedPropertyNames"
private const val KEY_EXCLUDE_FUNCTION_NAMES = "puml.excludedFunctionNames"
private const val KEY_ALLOW_EMPTY_PACKAGE = "puml.allowEmptyPackage"

// Enable/Disable individual visualisations
private const val KEY_SHOW_VISIBILITY_MODIFIERS = "puml.showVisibilityModifiers"
private const val KEY_MARK_EXTENSIONS = "puml.markExtensions"

// Show/Hide kotlin specific information in diagram
private const val KEY_SHOW_EXTENSIONS = "puml.showExtensions"

// Show/Hide inherited information
private const val KEY_SHOW_INHERITED_PROPERTIES = "puml.showInheritedProperties"
private const val KEY_SHOW_INHERITED_FUNCTIONS = "puml.showInheritedFunctions"

// Show/Hide information in diagram by visibility
private const val KEY_SHOW_PUBLIC_CLASSES = "puml.showPublicClasses"
private const val KEY_SHOW_PUBLIC_PROPERTIES = "puml.showPublicProperties"
private const val KEY_SHOW_PUBLIC_FUNCTIONS = "puml.showPublicFunctions"
private const val KEY_SHOW_INTERNAL_CLASSES = "puml.showInternalClasses"
private const val KEY_SHOW_INTERNAL_PROPERTIES = "puml.showInternalProperties"
private const val KEY_SHOW_INTERNAL_FUNCTIONS = "puml.showInternalFunctions"
private const val KEY_SHOW_PRIVATE_CLASSES = "puml.showPrivateClasses"
private const val KEY_SHOW_PRIVATE_PROPERTIES = "puml.showPrivateProperties"
private const val KEY_SHOW_PRIVATE_FUNCTIONS = "puml.showPrivateFunctions"

// Show/Hide relations in diagram
private const val KEY_SHOW_INHERITANCE = "puml.showInheritance"
private const val KEY_SHOW_PROPERTY_RELATIONS = "puml.showPropertyRelations"
private const val KEY_SHOW_FUNCTION_RELATIONS = "puml.showFunctionRelations"

// Others
private const val KEY_SHOW_PACKAGES = "puml.showPackages"

// Add custom puml content
private const val KEY_PREFIX = "puml.prefix"
private const val KEY_POSTFIX = "puml.postfix"
private const val KEY_TITLE = "puml.title"

internal val ALL_KEYS: List<String>
    get() = listOf(
        KEY_INCLUDED_PACKAGES,
        KEY_EXCLUDE_PACKAGES,
        KEY_EXCLUDE_CLASS_NAMES,
        KEY_EXCLUDE_PROPERTY_NAMES,
        KEY_EXCLUDE_FUNCTION_NAMES,
        KEY_SHOW_INHERITED_PROPERTIES,
        KEY_SHOW_INHERITED_FUNCTIONS,
        KEY_SHOW_VISIBILITY_MODIFIERS,
        KEY_SHOW_EXTENSIONS,
        KEY_MARK_EXTENSIONS,
        KEY_SHOW_PUBLIC_CLASSES,
        KEY_SHOW_PUBLIC_PROPERTIES,
        KEY_SHOW_PUBLIC_FUNCTIONS,
        KEY_SHOW_INTERNAL_CLASSES,
        KEY_SHOW_INTERNAL_PROPERTIES,
        KEY_SHOW_INTERNAL_FUNCTIONS,
        KEY_SHOW_PRIVATE_CLASSES,
        KEY_SHOW_PRIVATE_PROPERTIES,
        KEY_SHOW_PRIVATE_FUNCTIONS,
        KEY_SHOW_INHERITANCE,
        KEY_SHOW_PROPERTY_RELATIONS,
        KEY_SHOW_FUNCTION_RELATIONS,
        KEY_SHOW_PACKAGES,
        KEY_ALLOW_EMPTY_PACKAGE,
        KEY_PREFIX,
        KEY_POSTFIX,
        KEY_TITLE,
    )

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
    val showPackages: Boolean = false,
    val allowEmptyPackage: Boolean = true,
    val prefix: String = "",
    val postfix: String = "",
    val title: String = "",
) {
    constructor(kspProcessorOptions: Map<String, String>) : this(
        includedPackages = kspProcessorOptions[KEY_INCLUDED_PACKAGES]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList(),
        excludedPackages = kspProcessorOptions[KEY_EXCLUDE_PACKAGES]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList(),
        excludedClassNames = kspProcessorOptions[KEY_EXCLUDE_CLASS_NAMES]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList(),
        excludedPropertyNames = kspProcessorOptions[KEY_EXCLUDE_PROPERTY_NAMES]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList(),
        excludedFunctionNames = kspProcessorOptions[KEY_EXCLUDE_FUNCTION_NAMES]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: DEFAULT_EXCLUDED_FUNCTIONS,
        showVisibilityModifiers = kspProcessorOptions[KEY_SHOW_VISIBILITY_MODIFIERS]?.equals("true", true) ?: true,
        markExtensions = kspProcessorOptions[KEY_MARK_EXTENSIONS]?.equals("true", true) ?: true,
        showInheritedProperties = kspProcessorOptions[KEY_SHOW_INHERITED_PROPERTIES]?.equals("true", true) ?: false,
        showInheritedFunctions = kspProcessorOptions[KEY_SHOW_INHERITED_FUNCTIONS]?.equals("true", true) ?: false,
        showExtensions = kspProcessorOptions[KEY_SHOW_EXTENSIONS]?.equals("true", true) ?: true,
        showPublicClasses = kspProcessorOptions[KEY_SHOW_PUBLIC_CLASSES]?.equals("true", true) ?: true,
        showPublicProperties = kspProcessorOptions[KEY_SHOW_PUBLIC_PROPERTIES]?.equals("true", true) ?: true,
        showPublicFunctions = kspProcessorOptions[KEY_SHOW_PUBLIC_FUNCTIONS]?.equals("true", true) ?: true,
        showInternalClasses = kspProcessorOptions[KEY_SHOW_INTERNAL_CLASSES]?.equals("true", true) ?: true,
        showInternalProperties = kspProcessorOptions[KEY_SHOW_INTERNAL_PROPERTIES]?.equals("true", true) ?: true,
        showInternalFunctions = kspProcessorOptions[KEY_SHOW_INTERNAL_FUNCTIONS]?.equals("true", true) ?: true,
        showPrivateClasses = kspProcessorOptions[KEY_SHOW_PRIVATE_CLASSES]?.equals("true", true) ?: true,
        showPrivateProperties = kspProcessorOptions[KEY_SHOW_PRIVATE_PROPERTIES]?.equals("true", true) ?: true,
        showPrivateFunctions = kspProcessorOptions[KEY_SHOW_PRIVATE_FUNCTIONS]?.equals("true", true) ?: true,
        showInheritance = kspProcessorOptions[KEY_SHOW_INHERITANCE]?.equals("true", true) ?: true,
        showPropertyRelations = kspProcessorOptions[KEY_SHOW_PROPERTY_RELATIONS]?.equals("true", true) ?: true,
        showFunctionRelations = kspProcessorOptions[KEY_SHOW_FUNCTION_RELATIONS]?.equals("true", true) ?: true,
        showPackages = kspProcessorOptions[KEY_SHOW_PACKAGES]?.equals("true", true) ?: false,
        allowEmptyPackage = kspProcessorOptions[KEY_ALLOW_EMPTY_PACKAGE]?.equals("true", true) ?: false,
        prefix = kspProcessorOptions[KEY_PREFIX] ?: "",
        postfix = kspProcessorOptions[KEY_POSTFIX] ?: "",
        title = kspProcessorOptions[KEY_TITLE] ?: "",
    )

    fun asMap(): Map<String, String> {
        return mutableMapOf<String, String>().apply {
            put(KEY_INCLUDED_PACKAGES, includedPackages.joinToString(", "))
            put(KEY_EXCLUDE_PACKAGES, excludedPackages.joinToString(", "))
            put(KEY_EXCLUDE_CLASS_NAMES, excludedClassNames.joinToString(", "))
            put(KEY_EXCLUDE_PROPERTY_NAMES, excludedPropertyNames.joinToString(", "))
            put(KEY_EXCLUDE_FUNCTION_NAMES, excludedFunctionNames.joinToString(", "))
            put(KEY_SHOW_VISIBILITY_MODIFIERS, if (showVisibilityModifiers) "true" else "false")
            put(KEY_SHOW_INHERITED_PROPERTIES, if (showInheritedProperties) "true" else "false")
            put(KEY_SHOW_INHERITED_FUNCTIONS, if (showInheritedFunctions) "true" else "false")
            put(KEY_MARK_EXTENSIONS, if (markExtensions) "true" else "false")
            put(KEY_SHOW_EXTENSIONS, if (showExtensions) "true" else "false")
            put(KEY_SHOW_PUBLIC_CLASSES, if (showPublicClasses) "true" else "false")
            put(KEY_SHOW_PUBLIC_PROPERTIES, if (showPublicProperties) "true" else "false")
            put(KEY_SHOW_PUBLIC_FUNCTIONS, if (showPublicFunctions) "true" else "false")
            put(KEY_SHOW_INTERNAL_CLASSES, if (showInternalClasses) "true" else "false")
            put(KEY_SHOW_INTERNAL_PROPERTIES, if (showInternalProperties) "true" else "false")
            put(KEY_SHOW_INTERNAL_FUNCTIONS, if (showInternalFunctions) "true" else "false")
            put(KEY_SHOW_PRIVATE_CLASSES, if (showPrivateClasses) "true" else "false")
            put(KEY_SHOW_PRIVATE_PROPERTIES, if (showPrivateProperties) "true" else "false")
            put(KEY_SHOW_PRIVATE_FUNCTIONS, if (showPrivateFunctions) "true" else "false")
            put(KEY_SHOW_INHERITANCE, if (showInheritance) "true" else "false")
            put(KEY_SHOW_PROPERTY_RELATIONS, if (showPropertyRelations) "true" else "false")
            put(KEY_SHOW_FUNCTION_RELATIONS, if (showFunctionRelations) "true" else "false")
            put(KEY_SHOW_PACKAGES, if (showPackages) "true" else "false")
            put(KEY_ALLOW_EMPTY_PACKAGE, if (allowEmptyPackage) "true" else "false")
            put(KEY_PREFIX, prefix)
            put(KEY_POSTFIX, postfix)
            put(KEY_TITLE, title)
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

        packageName.isBlank() && !this.allowEmptyPackage -> {
            logger.v { "Exclude $packageName since empty Package deactivated by the options" }
            false
        }

        packageName.isBlank() && this.allowEmptyPackage -> true

        packageName.isNotBlank() && excludedPackages.isEmpty() && includedPackages.isEmpty() ->
            true

        this.excludedPackages.any { it == packageName } -> {
            logger.v { "Package is excluded: $packageName" }
            false
        }


        this.excludedPackages.any { (packageName.startsWith(it)) } -> {
            val packageThatExcludesThisClass = this.excludedPackages.find { (packageName.startsWith(it)) }
            logger.v { "Exclude package $packageName by excluded package: $packageThatExcludesThisClass" }
            false
        }

        this.includedPackages.isNotEmpty() && this.includedPackages.none { it == packageName || packageName.startsWith(it) } -> {
            logger.v { "Exclude package $packageName since it does not match the included packages: ${this.includedPackages.joinToString()}" }
            false
        }

        else -> true
    }
}.getOrElse { throwable ->
    logger.w { "Exclude package $packageName due to internal Kotlin error:\n${throwable.stackTraceToString()}" }
    false
}

fun Options?.isValid(type: KSType?, logger: KSPLogger? = null): Boolean = runCatching {
    when {
        this == null ->
            true

        type == null ->
            true


        type.declaration.isPublic() && !this.showPublicClasses -> {
            logger.v { "Exclude ${type.declaration.simpleName.getShortName()} since its Public and Public deactivated by the options" }
            false
        }

        type.declaration.isInternal() && !this.showInternalClasses -> {
            logger.v { "Exclude ${type.declaration.simpleName.getShortName()} since its Internal and Internal deactivated by the options" }
            false
        }

        type.declaration.isPrivate() && !this.showPrivateClasses -> {
            logger.v { "Exclude ${type.declaration.simpleName.getShortName()} since its Private and Private deactivated by the options" }
            false
        }

        !isValid(type.declaration.packageName.asString(), logger) -> false

        else -> true
    }
}.getOrElse { throwable ->
    logger.w { "Exclude ${type?.declaration?.simpleName?.asString()} due to internal Kotlin error:\n${throwable.stackTraceToString()}" }
    false
}

fun Options?.isValid(declaration: KSClassDeclaration, logger: KSPLogger? = null): Boolean = runCatching {
    when {
        this == null ->
            true

        declaration.isPublic() && !this.showPublicClasses -> {
            logger.v { "Exclude ${declaration.simpleName.getShortName()} since its Public and Public deactivated by the options" }
            false
        }

        declaration.isInternal() && !this.showInternalClasses -> {
            logger.v { "Exclude ${declaration.simpleName.getShortName()} since its Internal and Internal deactivated by the options" }
            false
        }

        declaration.isPrivate() && !this.showPrivateClasses -> {
            logger.v { "Exclude ${declaration.simpleName.getShortName()} since its Private and Private deactivated by the options" }
            false
        }

        declaration.simpleName.asString() in this.excludedClassNames -> {
            logger.v { "Exclude ${declaration.simpleName.asString()} since its in excludedClassNames" }
            false
        }

        !isValid(declaration.packageName.asString(), logger) -> false

        else -> true
    }
}.getOrElse { throwable ->
    logger.w { "Exclude ${declaration.simpleName.asString()} due to internal Kotlin error:\n${throwable.stackTraceToString()}" }
    false
}

fun Options?.isValid(declaration: KSPropertyDeclaration, logger: KSPLogger? = null): Boolean = runCatching {
    when {
        this == null ->
            true

        declaration.isPublic() && !this.showPublicProperties -> {
            logger.v { "Exclude ${declaration.simpleName.getShortName()} since its Public and Public deactivated by the options" }
            false
        }

        declaration.isInternal() && !this.showInternalProperties -> {
            logger.v { "Exclude ${declaration.simpleName.getShortName()} since its Internal and Internal deactivated by the options" }
            false
        }

        declaration.isPrivate() && !this.showPrivateProperties -> {
            logger.v { "Exclude ${declaration.simpleName.getShortName()} since its Private and Private deactivated by the options" }
            false
        }

        declaration.simpleName.asString() in this.excludedPropertyNames -> {
            logger.v { "Exclude ${declaration.simpleName.asString()} since its in excludedPropertyNames" }
            false
        }

        declaration.extensionReceiver != null && !this.showExtensions -> {
            logger.v { "Exclude ${declaration.simpleName.asString()} since Extension Functions are deactivated by the options" }
            false
        }

        else -> true
    }
}.getOrElse { throwable ->
    logger.w { "Exclude ${declaration.simpleName.asString()} due to internal Kotlin error:\n${throwable.stackTraceToString()}" }
    false
}

fun Options?.isValid(declaration: KSFunctionDeclaration, logger: KSPLogger? = null): Boolean = runCatching {
    when {
        this == null ->
            true

        declaration.isPublic() && !this.showPublicFunctions -> {
            logger.v { "Exclude ${declaration.simpleName.getShortName()} since its Public and Public deactivated by the options" }
            false
        }

        declaration.isInternal() && !this.showInternalFunctions -> {
            logger.v { "Exclude ${declaration.simpleName.getShortName()} since its Internal and Internal deactivated by the options" }
            false
        }

        declaration.isPrivate() && !this.showPrivateFunctions -> {
            logger.v { "Exclude ${declaration.simpleName.getShortName()} since its Private and Private deactivated by the options" }
            false
        }

        declaration.simpleName.asString() in this.excludedFunctionNames -> {
            logger.v { "Exclude ${declaration.simpleName.asString()} since its in excludedFunctionNames" }
            false
        }

        declaration.extensionReceiver != null && !this.showExtensions -> {
            logger.v { "Exclude ${declaration.simpleName.asString()} since Extension Functions are deactivated by the options" }
            false
        }

        else -> true
    }
}.getOrElse { throwable ->
    logger.w { "Exclude ${declaration.simpleName.asString()} due to internal Kotlin error:\n${throwable.stackTraceToString()}" }
    false
}

fun Sequence<KSPropertyDeclaration>.filterPropertiesByOptions(clazz: KSClassDeclaration, options: Options, logger: KSPLogger?): Sequence<KSPropertyDeclaration> {
    return if (!options.showInheritedProperties) {
        this.filter { options.isValid(it, logger) }
            .filter { property -> !property.isInheritedProperty(clazz, logger).also {
                logger.v { "filterPropertiesByOptions(): Filtered ${clazz.simpleName.asString()}.${property.simpleName.asString()} based on inheritance: $it" }
            } }
    } else {
        this.filter { options.isValid(it, logger) }
    }
}

fun Sequence<KSFunctionDeclaration>.filterFunctionsByOptions(clazz: KSClassDeclaration, options: Options, logger: KSPLogger?): Sequence<KSFunctionDeclaration> {
    return if (!options.showInheritedFunctions) {
        this.filter { options.isValid(it, logger) }
            .filter { function ->
                !function.isInheritedFunction(clazz, logger).also {
                    logger.v { "filterFunctionsByOptions(): Filtered ${clazz.simpleName.asString()}.${function.simpleName.asString()} based on inheritance: $it" }
                }
            }
    } else {
        this.filter { options.isValid(it, logger) }
    }
}