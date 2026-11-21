package graph

import toPumlAlias
import uml.element.DiagramElementBuilder

typealias Vertex = String
typealias Edge = Pair<Vertex, Vertex>

class RelationGraph {
    private val mutableRelations = mutableSetOf<Relation>()
    private val mutableElements = mutableSetOf<DiagramElementBuilder>()

    // The following indices are updated when an element or a relation is added,
    // so that the lookups do not depend on the size of the graph.
    private val buildersByName = mutableMapOf<String, DiagramElementBuilder>()
    private val vertexAliases = mutableSetOf<Vertex>()
    private val outRelations = mutableMapOf<Vertex, MutableList<Relation>>()
    private val inRelations = mutableMapOf<Vertex, MutableList<Relation>>()
    private val edgeSet = mutableSetOf<Edge>()

    val relations: Set<Relation> get() = mutableRelations
    val elements: Set<DiagramElementBuilder> get() = mutableElements

    val edges: List<Edge>
        get() = mutableRelations.map { it.fromAlias to it.toAlias }

    val vertices: Set<Vertex> get() = vertexAliases

    fun addElementBuilder(diagramElement: DiagramElementBuilder) {
        if (mutableElements.add(diagramElement)) {
            buildersByName.putIfAbsent(diagramElement.fullQualifiedName.withoutEmptyPackage(), diagramElement)
            vertexAliases.add(diagramElement.fullQualifiedName.toPumlAlias())
        }
    }

    /**
     * Replaces [oldElement] by [newElement] of the same class, which takes the position of the replaced one.
     */
    fun replaceElementBuilder(oldElement: DiagramElementBuilder, newElement: DiagramElementBuilder) {
        val replacedElements = mutableElements.map { if (it === oldElement) newElement else it }
        mutableElements.clear()
        mutableElements.addAll(replacedElements)
        buildersByName[newElement.fullQualifiedName.withoutEmptyPackage()] = newElement
    }

    fun addRelation(relation: Relation) {
        if (mutableRelations.add(relation)) {
            outRelations.getOrPut(relation.fromAlias) { mutableListOf() }.add(relation)
            inRelations.getOrPut(relation.toAlias) { mutableListOf() }.add(relation)
            edgeSet.add(relation.fromAlias to relation.toAlias)
        }
    }

    fun outDegreeOf(vertex: Vertex): Int {
        return outRelations[vertex]?.size ?: 0
    }

    fun inDegreeOf(vertex: Vertex): Int {
        return inRelations[vertex]?.size ?: 0
    }

    fun outEdgesOf(vertex: Vertex): List<Relation> {
        return outRelations[vertex].orEmpty()
    }

    fun inEdgesOf(vertex: Vertex): List<Relation> {
        return inRelations[vertex].orEmpty()
    }

    fun hasEdge(fromVertex: Vertex, toVertex: Vertex): Boolean {
        return (fromVertex to toVertex) in edgeSet
    }

    fun hasVertex(vertex: Vertex): Boolean = vertex.toPumlAlias() in vertexAliases

    fun findBuilderForVertex(vertex: Vertex): DiagramElementBuilder? {
        return buildersByName[vertex.withoutEmptyPackage()]
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
        .filter { vertex -> vertex !in outRelations && vertex !in inRelations }

    fun computeInvalidRelations(): List<Relation> = relations
        .filter { relation -> relation.fromAlias !in vertices || relation.toAlias !in vertices }

    // The name of a class without a package is ".Foo" if it is taken from its declaration and "Foo" if the class is referred to as supertype
    private fun String.withoutEmptyPackage(): String = trimStart('.')
}
