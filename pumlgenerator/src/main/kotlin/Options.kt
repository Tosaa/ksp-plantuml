import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration

data class Options(
    val excludedPackages: List<String> = emptyList(),
    val excludedFunctions: List<String> = DEFAULT_EXCLUDED_FUNCTIONS,
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
    val allowEmptyPackage: Boolean = true
) {
    constructor(kspProcessorOptions: Map<String, String>) : this(
        excludedPackages = kspProcessorOptions["excludedPackages"]?.split(",")?.map { it.trim() } ?: emptyList(),
        excludedFunctions = kspProcessorOptions["excludedFunctionNames"]?.split(",")?.map { it.trim() } ?: DEFAULT_EXCLUDED_FUNCTIONS,
        showPublicClasses = kspProcessorOptions["showPublicClasses"]?.equals("true", true) ?: true,
        showPublicProperties = kspProcessorOptions["showPublicProperties"]?.equals("true", true) ?: true,
        showPublicFunctions = kspProcessorOptions["showPublicFunctions"]?.equals("true", true) ?: true,
        showInternalClasses = kspProcessorOptions["showInternalClasses"]?.equals("true", true) ?: true,
        showInternalProperties = kspProcessorOptions["showInternalProperties"]?.equals("true", true) ?: true,
        showInternalFunctions = kspProcessorOptions["showInternalFunctions"]?.equals("true", true) ?: true,
        showPrivateClasses = kspProcessorOptions["showPrivateClasses"]?.equals("true", true) ?: true,
        showPrivateProperties = kspProcessorOptions["showPrivateProperties"]?.equals("true", true) ?: true,
        showPrivateFunctions = kspProcessorOptions["showPrivateFunctions"]?.equals("true", true) ?: true,
        showInheritance = kspProcessorOptions["showInheritance"]?.equals("true", true) ?: true,
        showRelations = kspProcessorOptions["showRelations"]?.equals("true", true) ?: true,
        showPackages = kspProcessorOptions["showPackages"]?.equals("true", true) ?: false,
    )

    fun asMap(): Map<String, String> {
        return mutableMapOf<String, String>().apply {
            put("excludedPackages", excludedPackages.joinToString(", "))
            put("excludedFunctionNames", excludedFunctions.joinToString(", "))
            put("showPublicClasses", if (showPublicClasses) "true" else "false")
            put("showPublicProperties", if (showPublicProperties) "true" else "false")
            put("showPublicFunctions", if (showPublicFunctions) "true" else "false")
            put("showInternalClasses", if (showInternalClasses) "true" else "false")
            put("showInternalProperties", if (showInternalProperties) "true" else "false")
            put("showInternalFunctions", if (showInternalFunctions) "true" else "false")
            put("showPrivateClasses", if (showPrivateClasses) "true" else "false")
            put("showPrivateProperties", if (showPrivateProperties) "true" else "false")
            put("showPrivateFunctions", if (showPrivateFunctions) "true" else "false")
            put("showInheritance", if (showInheritance) "true" else "false")
            put("showRelations", if (showRelations) "true" else "false")
            put("showPackages", if (showPackages) "true" else "false")
        }
    }

    companion object {
        val DEFAULT_EXCLUDED_FUNCTIONS = listOf("<init>", "toString", "equals", "hashCode")
    }
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

    declaration.packageName.asString().isEmpty() -> {
        if (!this.allowEmptyPackage) {
            logger.v { "Exclude ${declaration.simpleName.getShortName()} since empty Package is not allowed" }
            false
        } else {
            true
        }
    }

    declaration.packageName.asString() in this.excludedPackages -> {
        logger.v { "Exclude ${declaration.simpleName.getShortName()} since its Package is excluded: ${declaration.packageName.asString()}" }
        false
    }

    else -> true
}

fun Options?.isValid(declaration: KSPropertyDeclaration, logger: KSPLogger? = null): Boolean = when {
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

    else -> true
}

fun Options?.isValid(declaration: KSFunctionDeclaration, logger: KSPLogger? = null): Boolean = when {
    this == null ->
        true

    declaration.simpleName.asString() in this.excludedFunctions -> {
        logger.v { "Exclude ${declaration.simpleName.getShortName()} since its excluded by the options" }
        false
    }

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

    else -> true
}