import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType

data class Options(
    val excludedPackages: List<String> = emptyList(),
    val excludedPropertyNames: List<String> = emptyList(),
    val excludedFunctionNames: List<String> = DEFAULT_EXCLUDED_FUNCTIONS,
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
    val showRelations: Boolean = true,
    val showPackages: Boolean = false,
    val allowEmptyPackage: Boolean = true,
    val prefix: String = "",
    val postfix: String = "",
    val title: String = "",
) {
    constructor(kspProcessorOptions: Map<String, String>) : this(
        excludedPackages = kspProcessorOptions["puml.excludedPackages"]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList(),
        excludedPropertyNames = kspProcessorOptions["puml.excludedPropertyNames"]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList(),
        excludedFunctionNames = kspProcessorOptions["puml.excludedFunctionNames"]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: DEFAULT_EXCLUDED_FUNCTIONS,
        showPublicClasses = kspProcessorOptions["puml.showPublicClasses"]?.equals("true", true) ?: true,
        showPublicProperties = kspProcessorOptions["puml.showPublicProperties"]?.equals("true", true) ?: true,
        showPublicFunctions = kspProcessorOptions["puml.showPublicFunctions"]?.equals("true", true) ?: true,
        showInternalClasses = kspProcessorOptions["puml.showInternalClasses"]?.equals("true", true) ?: true,
        showInternalProperties = kspProcessorOptions["puml.showInternalProperties"]?.equals("true", true) ?: true,
        showInternalFunctions = kspProcessorOptions["puml.showInternalFunctions"]?.equals("true", true) ?: true,
        showPrivateClasses = kspProcessorOptions["puml.showPrivateClasses"]?.equals("true", true) ?: true,
        showPrivateProperties = kspProcessorOptions["puml.showPrivateProperties"]?.equals("true", true) ?: true,
        showPrivateFunctions = kspProcessorOptions["puml.showPrivateFunctions"]?.equals("true", true) ?: true,
        showInheritance = kspProcessorOptions["puml.showInheritance"]?.equals("true", true) ?: true,
        showRelations = kspProcessorOptions["puml.showRelations"]?.equals("true", true) ?: true,
        showPackages = kspProcessorOptions["puml.showPackages"]?.equals("true", true) ?: false,
        prefix = kspProcessorOptions["puml.prefix"] ?: "",
        postfix = kspProcessorOptions["puml.postfix"] ?: "",
        title = kspProcessorOptions["puml.title"] ?: "",
    )

    fun asMap(): Map<String, String> {
        return mutableMapOf<String, String>().apply {
            put("puml.excludedPackages", excludedPackages.joinToString(", "))
            put("puml.excludedPropertyNames", excludedPropertyNames.joinToString(", "))
            put("puml.excludedFunctionNames", excludedFunctionNames.joinToString(", "))
            put("puml.showPublicClasses", if (showPublicClasses) "true" else "false")
            put("puml.showPublicProperties", if (showPublicProperties) "true" else "false")
            put("puml.showPublicFunctions", if (showPublicFunctions) "true" else "false")
            put("puml.showInternalClasses", if (showInternalClasses) "true" else "false")
            put("puml.showInternalProperties", if (showInternalProperties) "true" else "false")
            put("puml.showInternalFunctions", if (showInternalFunctions) "true" else "false")
            put("puml.showPrivateClasses", if (showPrivateClasses) "true" else "false")
            put("puml.showPrivateProperties", if (showPrivateProperties) "true" else "false")
            put("puml.showPrivateFunctions", if (showPrivateFunctions) "true" else "false")
            put("puml.showInheritance", if (showInheritance) "true" else "false")
            put("puml.showRelations", if (showRelations) "true" else "false")
            put("puml.showPackages", if (showPackages) "true" else "false")
            put("puml.prefix", prefix)
            put("puml.postfix", postfix)
            put("puml.title", title)
        }
    }

    companion object {
        val DEFAULT_EXCLUDED_FUNCTIONS = listOf("<init>", "toString", "equals", "hashCode")
    }
}

fun Options?.isValid(packageName: String, logger: KSPLogger? = null): Boolean = when {
    this == null ->
        true

    packageName.isBlank() && !this.allowEmptyPackage -> {
        logger.v { "Exclude $packageName since empty Package is not allowed" }
        false
    }

    packageName.isNotBlank() && excludedPackages.isEmpty() ->
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

    else -> true
}

fun Options?.isValid(type: KSType?, logger: KSPLogger? = null): Boolean = when {
    this == null ->
        true

    type == null ->
        true


    type.declaration.isPublic() && !this.showPublicClasses -> {
        logger.v { "Exclude ${type.declaration.simpleName.getShortName()} since its Public and Public is not allowed" }
        false
    }

    type.declaration.isInternal() && !this.showInternalClasses -> {
        logger.v { "Exclude ${type.declaration.simpleName.getShortName()} since its Internal and Internal is not allowed" }
        false
    }

    type.declaration.isPrivate() && !this.showPrivateClasses -> {
        logger.v { "Exclude ${type.declaration.simpleName.getShortName()} since its Private and Private is not allowed" }
        false
    }

    !isValid(type.declaration.packageName.asString(), logger) -> false

    else -> true
}

fun Options?.isValid(declaration: KSClassDeclaration, logger: KSPLogger? = null): Boolean = when {
    this == null ->
        true

    declaration.isPublic() && !this.showPublicClasses -> {
        logger.v { "Exclude ${declaration.simpleName.getShortName()} since its Public and Public is not allowed" }
        false
    }

    declaration.isInternal() && !this.showInternalClasses -> {
        logger.v { "Exclude ${declaration.simpleName.getShortName()} since its Internal and Internal is not allowed" }
        false
    }

    declaration.isPrivate() && !this.showPrivateClasses -> {
        logger.v { "Exclude ${declaration.simpleName.getShortName()} since its Private and Private is not allowed" }
        false
    }

    !isValid(declaration.packageName.asString(), logger) -> false

    else -> true
}

fun Options?.isValid(declaration: KSPropertyDeclaration, logger: KSPLogger? = null): Boolean = when {
    this == null ->
        true

    declaration.isPublic() && !this.showPublicProperties -> {
        logger.v { "Exclude ${declaration.simpleName.getShortName()} since its Public and Public is not allowed" }
        false
    }

    declaration.isInternal() && !this.showInternalProperties -> {
        logger.v { "Exclude ${declaration.simpleName.getShortName()} since its Internal and Internal is not allowed" }
        false
    }

    declaration.isPrivate() && !this.showPrivateProperties -> {
        logger.v { "Exclude ${declaration.simpleName.getShortName()} since its Private and Private is not allowed" }
        false
    }

    declaration.simpleName.asString() in this.excludedPropertyNames -> {
        logger.v { "Exclude ${declaration.simpleName.asString()} since its in excludedPropertyNames" }
        false
    }

    else -> true
}

fun Options?.isValid(declaration: KSFunctionDeclaration, logger: KSPLogger? = null): Boolean = when {
    this == null ->
        true

    declaration.isPublic() && !this.showPublicFunctions -> {
        logger.v { "Exclude ${declaration.simpleName.getShortName()} since its Public and Public is not allowed" }
        false
    }

    declaration.isInternal() && !this.showInternalFunctions -> {
        logger.v { "Exclude ${declaration.simpleName.getShortName()} since its Internal and Internal is not allowed" }
        false
    }

    declaration.isPrivate() && !this.showPrivateFunctions -> {
        logger.v { "Exclude ${declaration.simpleName.getShortName()} since its Private and Private is not allowed" }
        false
    }

    declaration.simpleName.asString() in this.excludedFunctionNames -> {
        logger.v { "Exclude ${declaration.simpleName.asString()} since its in excludedFunctionNames" }
        false
    }

    else -> true
}