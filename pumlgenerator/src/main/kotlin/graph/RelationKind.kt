package graph

import Notation

/**
 * Maps code constructs to the relations of a class diagram and to their arrows in each [Notation].
 *
 * The order of the entries is the priority of the relations in the notation of UML:
 * if more than one relation exists between two classes, only the first one is drawn.
 * The notation of plantuml keeps the priority of the earlier versions, see [plantumlPriority].
 *
 * The notation of UML uses neither aggregation nor composition, because the source code does not tell whether a class owns the objects it refers to.
 *
 * @param umlArrow the arrow in the notation of UML
 * @param plantumlArrow the arrow in the notation of plantuml
 * @param plantumlPriority the priority in the notation of plantuml, the lowest one is drawn first.
 * Relations that were not distinguished by the earlier versions have the same priority.
 */
enum class RelationKind(private val umlArrow: String, private val plantumlArrow: String, private val plantumlPriority: Int) {
    // Generalization: a class extends a class or an interface extends an interface
    Inheritance(umlArrow = "--|>", plantumlArrow = "--|>", plantumlPriority = 0),

    // Realization: a class or an object implements an interface
    Realization(umlArrow = "..|>", plantumlArrow = "--|>", plantumlPriority = 0),

    // Association: a property has the type of another class
    Property(umlArrow = "->", plantumlArrow = "-*", plantumlPriority = 1),

    // Association with multiplicity *: a property is a collection or an array of another class
    CollectionProperty(umlArrow = "->", plantumlArrow = ".*", plantumlPriority = 3),

    // Dependency: the type of a property refers to another class as type argument, e.g. Result<T>
    IndirectProperty(umlArrow = ".>", plantumlArrow = ".*", plantumlPriority = 3),

    // Dependency: a function returns another class
    Function(umlArrow = ".>", plantumlArrow = "->", plantumlPriority = 2),

    // Dependency: the return type of a function refers to another class as type argument
    IndirectFunction(umlArrow = ".>", plantumlArrow = ".>", plantumlPriority = 4);

    fun arrow(notation: Notation): String = when (notation) {
        Notation.PLANTUML -> plantumlArrow
        Notation.UML -> umlArrow
    }

    fun priority(notation: Notation): Int = when (notation) {
        Notation.PLANTUML -> plantumlPriority
        Notation.UML -> ordinal
    }

    /**
     * arrowWithLevel enables positioning elements on the same level (level = 0) or below each other (level > 0)
     */
    fun arrowWithLevel(notation: Notation, level: Int = 0): String = arrow(notation).let { it.first().toString().repeat(level) + it }

    /**
     * The arrow from the end to the start of the relation, e.g. `<|--` for `--|>`.
     * It is used to draw `Parent <|-- Child`, so that the parent is placed above the child.
     */
    fun reversedArrow(notation: Notation): String = arrow(notation).reversed().replace('>', '<')
}
