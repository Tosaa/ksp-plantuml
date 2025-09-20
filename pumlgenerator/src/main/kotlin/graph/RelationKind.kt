package graph

enum class RelationKind(val arrow: String, val reversedArrow: String = arrow.reversed()) {
    // Properties will be shown as aggregation
    Property("-*"),

    // Functions will be shown as dependency
    Function("->"),

    // Indirect properties will be shown as aggregation but with dotted line
    IndirectProperty(".*"),

    // Indirect functions will be shown as dependency but with dotted line
    IndirectFunction(".>"),

    // Inheritance will be shown as inheritance
    Inheritance("--|>", "<|--");

    /**
     * arrowWithLevel enables positioning elements on the same level (level = 0) or below each other (level > 0)
     */
    fun arrowWithLevel(level: Int = 0): String = arrow.first().toString().repeat(level) + arrow
}