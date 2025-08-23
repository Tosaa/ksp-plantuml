package uml.relation

data class SimpleElementRelation(val headAlias: String, val tailAlias: String, val relationKind: RelationKind, val text:String = "") : ElementRelation{
    override fun render(): String {
        val textOnArrow = text.takeIf { it.isNotEmpty() }?.let { ": $it" } ?: ""
        return """$headAlias ${relationKind.arrow} $tailAlias $textOnArrow""".trim()
    }
}
