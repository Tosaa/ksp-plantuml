package uml

sealed class ElementKind(val kind: String, val kindExtra: String) {
    class CLAZZ(isSealed: Boolean) : ElementKind("class", if (isSealed) "<<Sealed>>" else "")
    class INTERFACE(isSealed: Boolean) : ElementKind("interface", if (isSealed) "<<Sealed>>" else "")
    data object ENUM : ElementKind("enum", "")
    data object OBJECT : ElementKind("class", "<< (O, #FF7700) object>>")
}