package uml.relation

data class GenericElementRelation(val headAlias: String, val diamondAlias: String, val tailAliases: List<String>, val relationKind: RelationKind, val text: String) : ElementRelation {
    override fun render(): String {
        val tails = tailAliases.joinToString("\n") { "$diamondAlias ${relationKind.arrow} $it" }
        return """
<> $diamondAlias
$headAlias - $diamondAlias : $text
$tails
            """.trimIndent()
    }
}
