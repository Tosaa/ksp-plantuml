package graph

private typealias Vertex = String
private typealias Edge = Pair<Vertex, Vertex>

class RelationGraph {
    val relations = mutableSetOf<Relation>()

    val edges: List<Edge>
        get() = this.relations.map { it.fromAlias to it.toAlias }

    val vertices: Set<Vertex>
        get() = (this.relations.map { it.fromAlias } + this.relations.map { it.toAlias }).toSet()

    fun addRelation(relation: Relation) {
        this.relations.add(relation)
    }

    fun outDegreeOf(vertex: Vertex): Int {
        return this.edges.count { it.first == vertex }
    }

    fun inDegreeOf(vertex: Vertex): Int {
        return this.edges.count { it.second == vertex }
    }


    fun outEdgesOf(vertex: Vertex): List<Relation> {
        return this.relations.filter { it.fromAlias == vertex }
    }

    fun inEdgesOf(vertex: Vertex): List<Relation> {
        return this.relations.filter { it.toAlias == vertex }
    }

    fun hasEdge(fromVertex: Vertex, toVertex: Vertex): Boolean {
        return edges.contains(fromVertex to toVertex)
    }


}