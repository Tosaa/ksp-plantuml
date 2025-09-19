package graph

enum class RelationKind(val arrow: String, val reversedArrow: String =arrow.reversed()) {
    Property("-*"),
    Function("->"),
    IndirectProperty(".*"),
    IndirectFunction(".>"),
    Inheritance("--|>", "<|--");

    fun arrowWithLevel(level: Int = 0): String = arrow.first().toString().repeat(level) + arrow
}