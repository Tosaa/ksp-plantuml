package graph

import uml.element.DiagramElementBuilder

typealias Vertex = String
typealias Edge = Pair<Vertex, Vertex>

class RelationGraph {
    val relations = mutableSetOf<Relation>()
    val elements = mutableSetOf<DiagramElementBuilder>()

    val edges: List<Edge>
        get() = this.relations.map { it.fromAlias to it.toAlias }

    val vertices: Set<Vertex>
        get() = elements.map { it.fullQualifiedName.replace(".", "_").trim('_') }.toSet()

    fun addElementBuilder(diagramElement: DiagramElementBuilder) {
        this.elements.add(diagramElement)
    }

    /**
     * Replaces [oldElement] by [newElement] of the same class, which takes the position of the replaced one.
     */
    fun replaceElementBuilder(oldElement: DiagramElementBuilder, newElement: DiagramElementBuilder) {
        val replacedElements = elements.map { if (it === oldElement) newElement else it }
        elements.clear()
        elements.addAll(replacedElements)
    }

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

    fun hasVertex(vertex: Vertex): Boolean = vertex.replace(".", "_").trim('_') in vertices

    fun findBuilderForVertex(vertex: Vertex): DiagramElementBuilder? {
        return elements.find { it.fullQualifiedName.withoutEmptyPackage() == vertex.withoutEmptyPackage() }
    }

    fun describe(): String {
        return """
            Vertices: ${vertices.size}
            Edges: ${edges.size}
            Unrelated Vertices: ${computeVerticesWithoutRelations().joinToString()}
            Invalid Relations: ${computeInvalidRelations().joinToString()}
        """.trimIndent()
    }

    fun computeVerticesWithoutRelations(): List<Vertex> = vertices
        .filter { vertex -> vertex !in (edges.map { it.first } + edges.map { it.second }) }

    fun computeInvalidRelations(): List<Relation> = relations
        .filter { relation -> relation.fromAlias !in vertices || relation.toAlias !in vertices }

    // The name of a class without a package is ".Foo" if it is taken from its declaration and "Foo" if the class is referred to as supertype
    private fun String.withoutEmptyPackage(): String = trimStart('.')
}
