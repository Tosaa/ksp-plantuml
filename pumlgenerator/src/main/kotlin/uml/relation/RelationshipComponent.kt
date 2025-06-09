package uml.relation

import Options
import uml.Renderable

interface RelationshipComponent : Renderable {
    interface Builder<T : RelationshipComponent> {
        val options: Options?
        fun build(): T?
    }
}
