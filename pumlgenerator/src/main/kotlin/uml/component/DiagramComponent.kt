package uml.component

import Options
import uml.Renderable


interface DiagramComponent : Renderable {
    val attributes: List<ClassAttribute>

    interface Builder<T : DiagramComponent> {
        val options: Options?
        fun build(): T?
    }

    companion object {
        val INDENT = "\t"
        val PROPERTY_INDENT = "\t\t"
        val FUNCTION_INDENT = "\t\t"
    }
}