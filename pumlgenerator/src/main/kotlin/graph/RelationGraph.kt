package graph

class RelationGraph {
    val relations = mutableSetOf<Relation>()

    val edges: List<Pair<String, String>>
        get() = this.relations.map { it.fromAlias to it.toAlias }

    val vertices: Set<String>
        get() = (this.relations.map { it.fromAlias } + this.relations.map { it.toAlias }).toSet()

    fun addRelation(relation: Relation) {
        this.relations.add(relation)
    }

    fun graphAsText(): String {
        return buildString {
            appendLine("Vertices: ${vertices.size}, Edges: ${edges.size}")
            vertices.forEach {
                appendLine("$it: out-degree=${outDegreeOf(it)}, in-degree=${inDegreeOf(it)}")
            }
            edges.forEach {
                appendLine("${it.first} -- ${it.second}")
            }
        }
    }

    fun outDegreeOf(alias: String): Int {
        return this.edges.count { it.first == alias }
    }

    fun inDegreeOf(alias: String): Int {
        return this.edges.count { it.second == alias }
    }


    fun outEdgesOf(alias: String): List<Relation> {
        return this.relations.filter { it.fromAlias == alias }
    }

    fun inEdgesOf(alias: String): List<Relation> {
        return this.relations.filter { it.toAlias == alias }
    }


}