package uml

import Notation

/**
 * @param kind declares the element in plantuml, e.g. `class` or `enum`
 * @param stereotypes the stereotypes of the element in the notation of plantuml, e.g. `<<Sealed>>`
 * @param umlStereotypes the stereotypes of the element in the notation of UML.
 * They start with the keyword of UML for the kind of the element, e.g. `<<interface, Sealed>>`, which the notation of plantuml shows as circled character instead.
 */
sealed class ElementKind(val kind: String, private val stereotypes: String, private val umlStereotypes: String = stereotypes) {
    class CLAZZ(isAbstract: Boolean, isSealed: Boolean, isData: Boolean, isValue: Boolean) : ElementKind(
        if (isAbstract) "abstract class" else "class",
        stereotypesOf("Sealed" to isSealed, "Data" to isData, "Value" to isValue)
    )

    class INTERFACE(isSealed: Boolean, isFunctional: Boolean) : ElementKind(
        "interface",
        stereotypesOf("Sealed" to isSealed, "Fun" to isFunctional),
        stereotypesOf("interface" to true, "Sealed" to isSealed, "Fun" to isFunctional)
    )

    data object ENUM : ElementKind("enum", "", "<<enumeration>>")
    class OBJECT(isData: Boolean) : ElementKind("class", if (isData) "<< (O, #FF7700) data object>>" else "<< (O, #FF7700) object>>")
    data object ALIAS : ElementKind("class", "<< (A, #FF7700) alias>>")
    data object ANNOTATION : ElementKind("annotation", "", "<<annotation>>")

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
