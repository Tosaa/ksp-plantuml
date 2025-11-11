/**
 * Kind of a class declaration.
 * Interface, class, enum class and object
 * are all considered a class declaration.
 */
enum class ClassKind(val type: String) {
    INTERFACE("interface"),
    CLASS("class"),
    ENUM_CLASS("enum_class"),
    ENUM_ENTRY("enum_entry"),
    OBJECT("object"),
    ANNOTATION_CLASS("annotation_class")
}