package uml

import Notation

/**
 * @param kind declares the element in plantuml, e.g. `class` or `enum`
 * @param stereotypes the stereotypes of the element in the notation of plantuml, e.g. `<<Sealed>>`
 * @param umlStereotypes the stereotypes of the element in the notation of UML.
 * They start with the keyword of UML for the kind of the element, e.g. `<<interface, Sealed>>`, which the notation of plantuml shows as circled character instead.
 */
sealed class ElementKind(val kind: String, private val stereotypes: String, private val umlStereotypes: String = stereotypes) {
    class CLAZZ(isSealed: Boolean, isData: Boolean) : ElementKind(
        "class",
        stereotypesOf("Sealed" to isSealed, "Data" to isData)
    )

    class INTERFACE(isSealed: Boolean) : ElementKind(
        "interface",
        stereotypesOf("Sealed" to isSealed),
        stereotypesOf("interface" to true, "Sealed" to isSealed)
    )

    data object ENUM : ElementKind("enum", "", "<<enumeration>>")
    data object OBJECT : ElementKind("class", "<< (O, #FF7700) object>>")
    data object ALIAS : ElementKind("class", "<< (A, #FF7700) alias>>")

    fun kindExtra(notation: Notation): String = when (notation) {
        Notation.PLANTUML -> stereotypes
        Notation.UML -> umlStereotypes
    }

    companion object {
        /**
         * UML keywords like `interface` and stereotypes like `Data` are written in the same way.
         *
         * @return all names that apply as one comma separated list in guillemets, e.g. `<<interface, Sealed>>`
         */
        private fun stereotypesOf(vararg stereotypes: Pair<String, Boolean>): String = stereotypes
            .filter { it.second }
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator = ", ", prefix = "<<", postfix = ">>") { it.first }
            ?: ""
    }
}
