/**
 * The notation in which the diagram is drawn. It is selected with the option `puml.notation`.
 *
 * The notation decides how the elements and relations are drawn, not which of them are drawn.
 */
enum class Notation(val style: String) {

    /**
     * The notation of the earlier versions, which relies on the defaults of plantuml:
     * - The kind of an element is a circled character and the visibility of a member is an icon.
     * - Properties are lines that end with a diamond, functions are lines that end with an arrow.
     * - Functions list the types of their parameters.
     */
    PLANTUML(style = ""),

    /**
     * The notation of UML class diagrams:
     * - The kind of an element is a keyword like `<<interface>>` and the visibility of a member is one of `+`, `-`, `#` and `~`.
     * - Properties are associations with a multiplicity, functions are dependencies.
     * - Functions name their parameters.
     *
     * [style] hides the circled characters and the visibility icons of plantuml.
     */
    UML(style = "hide circle\nskinparam classAttributeIconSize 0");

    companion object {
        val DEFAULT = PLANTUML

        /**
         * @param name name of a notation, the case is ignored and a blank name selects [DEFAULT]
         * @throws IllegalArgumentException if there is no notation with the given name
         */
        fun fromName(name: String): Notation {
            val notationName = name.trim().uppercase().ifEmpty { DEFAULT.name }
            return requireNotNull(entries.find { it.name == notationName }) {
                "Unknown puml.notation '$name'. Available notations: ${entries.joinToString()}"
            }
        }
    }
}
