enum class OptionConstants(val identifier: String) {

    // Active includes/excludes
    KEY_INCLUDED_PACKAGES("puml.includedPackages"),
    KEY_EXCLUDE_PACKAGES("puml.excludedPackages"),
    KEY_EXCLUDE_CLASS_NAMES("puml.excludedClassNames"),
    KEY_EXCLUDE_PROPERTY_NAMES("puml.excludedPropertyNames"),
    KEY_EXCLUDE_FUNCTION_NAMES("puml.excludedFunctionNames"),
    KEY_ALLOW_EMPTY_PACKAGE("puml.allowEmptyPackage"),

    // Enable/Disable individual visualisations
    KEY_SHOW_VISIBILITY_MODIFIERS("puml.showVisibilityModifiers"),
    KEY_MARK_EXTENSIONS("puml.markExtensions"),

    // Show/Hide kotlin specific information in diagram
    KEY_SHOW_EXTENSIONS("puml.showExtensions"),

    // Show/Hide inherited information
    KEY_SHOW_INHERITED_PROPERTIES("puml.showInheritedProperties"),
    KEY_SHOW_INHERITED_FUNCTIONS("puml.showInheritedFunctions"),

    // Show/Hide information in diagram by visibility
    KEY_SHOW_PUBLIC_CLASSES("puml.showPublicClasses"),
    KEY_SHOW_PUBLIC_PROPERTIES("puml.showPublicProperties"),
    KEY_SHOW_PUBLIC_FUNCTIONS("puml.showPublicFunctions"),
    KEY_SHOW_INTERNAL_CLASSES("puml.showInternalClasses"),
    KEY_SHOW_INTERNAL_PROPERTIES("puml.showInternalProperties"),
    KEY_SHOW_INTERNAL_FUNCTIONS("puml.showInternalFunctions"),
    KEY_SHOW_PRIVATE_CLASSES("puml.showPrivateClasses"),
    KEY_SHOW_PRIVATE_PROPERTIES("puml.showPrivateProperties"),
    KEY_SHOW_PRIVATE_FUNCTIONS("puml.showPrivateFunctions"),

    // Show/Hide relations in diagram
    KEY_SHOW_INHERITANCE("puml.showInheritance"),
    KEY_SHOW_PROPERTY_RELATIONS("puml.showPropertyRelations"),
    KEY_SHOW_FUNCTION_RELATIONS("puml.showFunctionRelations"),
    KEY_SHOW_INDIRECT_RELATIONS("puml.showIndirectRelations"),
    KEY_MAX_RELATIONS("puml.maxRelations"),

    // Others
    KEY_SHOW_PACKAGES("puml.showPackages"),

    // Add custom puml content
    KEY_PREFIX("puml.prefix"),
    KEY_POSTFIX("puml.postfix"),
    KEY_TITLE("puml.title"),

    // Save to file within generated resources folder
    KEY_OUTPUT_FILE_PATH("puml.outputFileName");

    companion object {
        val MAX_RELATIONS = 6
        val IDENTIFIER = entries.map { it.identifier }
    }
}