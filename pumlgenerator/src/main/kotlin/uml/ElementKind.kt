package uml

sealed class ElementKind(val kind: String, val kindExtra: String) {
    class CLAZZ(isSealed: Boolean, isData: Boolean) : ElementKind(
        "class", when {
            isSealed && isData -> "<<Sealed, Data>>"
            isData -> "<<Data>>"
            isSealed -> "<<Sealed>>"
            else -> ""
        }
    )

    class INTERFACE(isSealed: Boolean) : ElementKind("interface", if (isSealed) "<<Sealed>>" else "")
    data object ENUM : ElementKind("enum", "")
    data object OBJECT : ElementKind("class", "<< (O, #FF7700) object>>")
    data object ALIAS : ElementKind("class", "<< (A, #FF7700) alias>>")
}