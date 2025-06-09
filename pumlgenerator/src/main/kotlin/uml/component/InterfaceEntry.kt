package uml.component

import Options
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import isValid
import uml.className
import uml.component.DiagramComponent.Companion.FUNCTION_INDENT
import uml.component.DiagramComponent.Companion.PROPERTY_INDENT
import uml.fullQualifiedName

data class InterfaceEntry(val uniqueIdentifier: String, val className: String, val classAlias: String, override val attributes: List<ClassAttribute>, val functions: List<ClassFunction>, val isSealedClass: Boolean) : DiagramComponent {
    override fun render(): String {
        val attributesString = attributes
            .takeIf { it.isNotEmpty() }
            ?.let { it.joinToString(prefix = "\n", separator = "\n") { "$PROPERTY_INDENT${it.render()}" } }
            ?: ""
        val functionsString = functions
            .takeIf { it.isNotEmpty() }
            ?.let { it.joinToString(prefix = "\n", separator = "\n") { "$FUNCTION_INDENT${it.render()}" } }
            ?: ""
        val sealedClassIndicator = if (isSealedClass) {
            "<<Sealed>> "
        } else {
            ""
        }
        return """
    '$uniqueIdentifier            
    interface "$className" as $classAlias $sealedClassIndicator{$attributesString$functionsString
    }
"""
    }

    class Builder(val clazz: KSClassDeclaration, override val options: Options? = null) : DiagramComponent.Builder<InterfaceEntry> {
        override fun build(): InterfaceEntry? {
            return if (options.isValid(clazz)) {
                InterfaceEntry(
                    uniqueIdentifier = clazz.fullQualifiedName,
                    className = clazz.className,
                    classAlias = clazz.fullQualifiedName.replace(".", "_").trim('_'),
                    attributes = clazz.getAllProperties().filter { options?.isValid(it) == true }.map { it.toAttribute() }.toList(),
                    functions = clazz.getAllFunctions().filter { options?.isValid(it) == true }.map { it.toFunction() }.toList(),
                    isSealedClass = clazz.modifiers.contains(Modifier.SEALED)
                )
            } else {
                null
            }
        }
    }
}
