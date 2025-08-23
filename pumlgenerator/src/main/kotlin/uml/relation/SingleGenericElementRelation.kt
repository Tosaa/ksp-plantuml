package uml.relation

import uml.element.Type

class SuperGenericElementRelation(val headAlias: String, val type: Type, val relationKind: RelationKind, val text: String = "") : ElementRelation {

    override fun render(): String {
        return buildString {
            val genericTypes: MutableMap<Type, String?> = mutableMapOf()
            if (type.genericTypes.size > 1) {
                val diamondAlias = "d_${headAlias.replace(":","_")}_${type.fullQualifiedName.replace(".", "_").trim('_')}"
                appendLine("<> $diamondAlias")
                appendLine("$headAlias - $diamondAlias")
                genericTypes.put(type, diamondAlias)
            } else if (type.genericTypes.size == 1) {
                genericTypes.put(type, null)
            }
            while (genericTypes.isNotEmpty()) {
                val (type, diamond) = genericTypes.entries.first()
                genericTypes.remove(type)
                if (type.genericTypes.size > 1) {
                    val diamondAlias = "d_${headAlias.replace(":","_")}_${type.fullQualifiedName.replace(".", "_").trim('_')}"
                    appendLine("<> $diamondAlias")
                    type.genericTypes.forEach {
                        genericTypes.put(it, diamondAlias)
                    }
                    if (diamond != null) {
                        appendLine("$diamond - $diamondAlias")
                    }
                } else if (type.genericTypes.size == 1) {
                    genericTypes.put(type.genericTypes.first(), diamond)
                } else {
                    if (diamond != null) {
                        appendLine("$diamond ${relationKind.arrow} ${type.fullQualifiedName.replace(".", "_").trim('_')}")
                    } else {
                        appendLine("$headAlias ${relationKind.arrow} ${type.fullQualifiedName.replace(".", "_").trim('_')}")
                    }
                }
            }
        }
    }

}