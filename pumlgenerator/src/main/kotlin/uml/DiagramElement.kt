package uml

import Options
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration

/**
 * Example:
 * ElementKind "elementName" as elementAlias [ElementKind extras] {
 * ElementContent
 * }
 */
abstract class DiagramElement : Renderable {
    open val comment: String = ""
    abstract val elementName: String
    abstract val elementAlias: String
    abstract val elementKind: ElementKind

    abstract fun getContent(indent: String): String

    override fun render(): String {
        return """
    ${comment.ensureStartsWith('\'')}
    ${elementKind.kind} "$elementName" as $elementAlias ${elementKind.kindExtra}{
        ${getContent("$INDENT$INDENT").trim()}
    }
"""
    }

    interface Builder<T : DiagramElement> {
        val properties: MutableList<KSPropertyDeclaration>
        val functions: MutableList<KSFunctionDeclaration>
        val clazz: KSClassDeclaration
        val options: Options?
        fun build(): T?
    }

    private fun String.ensureStartsWith(char: Char): String = if (this.startsWith(char)) this else char + this

    companion object {
        val INDENT = "\t"
        val PROPERTY_INDENT = "\t\t"
        val FUNCTION_INDENT = "\t\t"
    }
}