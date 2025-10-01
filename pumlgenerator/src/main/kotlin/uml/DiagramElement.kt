package uml

import Options
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import ensureStartsWith

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
        val fullQualifiedName: String
        var isShell: Boolean
        val allProperties: List<KSPropertyDeclaration>
        val allFunctions: List<KSFunctionDeclaration>
        val clazz: KSClassDeclaration
        val options: Options?
        fun build(): DiagramElement?
        fun addExtensionFunction(function:KSFunctionDeclaration)
        fun addExtensionProperty(property:KSPropertyDeclaration)
    }


    companion object {
        val INDENT = "\t"
        val PROPERTY_INDENT = "\t\t"
        val FUNCTION_INDENT = "\t\t"
        val shellString = "...\n=="
    }
}