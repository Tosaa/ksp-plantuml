package graph

enum class RelationKind(val arrow: String, val reversedArrow: String =arrow.reversed()) {
    Inheritance("--|>", "<|--"),
    Property("-*"),
    IndirectProperty(".*"),
    Function("->"),
    IndirectFunction(".>");

    fun arrowWithLevel(level: Int = 0): String = arrow.first().toString().repeat(level) + arrow
}