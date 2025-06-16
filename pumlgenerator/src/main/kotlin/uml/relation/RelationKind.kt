package uml.relation

enum class RelationKind(val arrow: String) {
    Inheritance("<|--"),
    Aggregation("*--"),
    Property("--*")
}