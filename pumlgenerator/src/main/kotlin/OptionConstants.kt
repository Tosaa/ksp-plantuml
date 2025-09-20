public object OptionConstants {

    // Active includes/excludes
    public const val KEY_INCLUDED_PACKAGES = "puml.includedPackages"
    public const val KEY_EXCLUDE_PACKAGES = "puml.excludedPackages"
    public const val KEY_EXCLUDE_CLASS_NAMES = "puml.excludedClassNames"
    public const val KEY_EXCLUDE_PROPERTY_NAMES = "puml.excludedPropertyNames"
    public const val KEY_EXCLUDE_FUNCTION_NAMES = "puml.excludedFunctionNames"
    public const val KEY_ALLOW_EMPTY_PACKAGE = "puml.allowEmptyPackage"

    // Enable/Disable individual visualisations
    public const val KEY_SHOW_VISIBILITY_MODIFIERS = "puml.showVisibilityModifiers"
    public const val KEY_MARK_EXTENSIONS = "puml.markExtensions"

    // Show/Hide kotlin specific information in diagram
    public const val KEY_SHOW_EXTENSIONS = "puml.showExtensions"

    // Show/Hide inherited information
    public const val KEY_SHOW_INHERITED_PROPERTIES = "puml.showInheritedProperties"
    public const val KEY_SHOW_INHERITED_FUNCTIONS = "puml.showInheritedFunctions"

    // Show/Hide information in diagram by visibility
    public const val KEY_SHOW_PUBLIC_CLASSES = "puml.showPublicClasses"
    public const val KEY_SHOW_PUBLIC_PROPERTIES = "puml.showPublicProperties"
    public const val KEY_SHOW_PUBLIC_FUNCTIONS = "puml.showPublicFunctions"
    public const val KEY_SHOW_INTERNAL_CLASSES = "puml.showInternalClasses"
    public const val KEY_SHOW_INTERNAL_PROPERTIES = "puml.showInternalProperties"
    public const val KEY_SHOW_INTERNAL_FUNCTIONS = "puml.showInternalFunctions"
    public const val KEY_SHOW_PRIVATE_CLASSES = "puml.showPrivateClasses"
    public const val KEY_SHOW_PRIVATE_PROPERTIES = "puml.showPrivateProperties"
    public const val KEY_SHOW_PRIVATE_FUNCTIONS = "puml.showPrivateFunctions"

    // Show/Hide relations in diagram
    public const val KEY_SHOW_INHERITANCE = "puml.showInheritance"
    public const val KEY_SHOW_PROPERTY_RELATIONS = "puml.showPropertyRelations"
    public const val KEY_SHOW_FUNCTION_RELATIONS = "puml.showFunctionRelations"

    //
    public const val KEY_SHOW_INDIRECT_RELATIONS = "puml.showIndirectRelations"
    public const val KEY_MAX_RELATIONS = "puml.maxRelations"
    public const val MAX_RELATIONS = 6


    // Others
    public const val KEY_SHOW_PACKAGES = "puml.showPackages"

    // Add custom puml content
    public const val KEY_PREFIX = "puml.prefix"
    public const val KEY_POSTFIX = "puml.postfix"
    public const val KEY_TITLE = "puml.title"

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
            KEY_SHOW_INDIRECT_RELATIONS,
            KEY_MAX_RELATIONS,
            KEY_SHOW_PACKAGES,
            KEY_ALLOW_EMPTY_PACKAGE,
            KEY_PREFIX,
            KEY_POSTFIX,
            KEY_TITLE,
        )
}