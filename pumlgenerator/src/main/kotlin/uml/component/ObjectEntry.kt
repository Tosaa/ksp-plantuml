package uml.component

import Options
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import isValid
import uml.className
import uml.component.DiagramComponent.Companion.FUNCTION_INDENT
import uml.component.DiagramComponent.Companion.PROPERTY_INDENT
import uml.fullQualifiedName

data class ObjectEntry(val uniqueIdentifier: String, val className: String, val classAlias: String, override val attributes: List<ClassAttribute>, val functions: List<ClassFunction>) : DiagramComponent {
    override fun render(): String {
        val attributesString = attributes
            .takeIf { it.isNotEmpty() }
            ?.let { it.joinToString(prefix = "\n", separator = "\n") { "$PROPERTY_INDENT${it.render()}" } }
            ?: ""
        val functionsString = functions
            .takeIf { it.isNotEmpty() }
            ?.let { it.joinToString(prefix = "\n", separator = "\n") { "$FUNCTION_INDENT${it.render()}" } }
            ?: ""
        return """
    '$uniqueIdentifier            
    class "$className" as $classAlias << (O, #FF7700) object>> {$attributesString$functionsString
    }
"""
    }

    class Builder(val clazz: KSClassDeclaration, override val options: Options? = null, val logger: KSPLogger?) : DiagramComponent.Builder<ObjectEntry> {
        override fun build(): ObjectEntry? {
            return if (options.isValid(clazz, logger)) {
                ObjectEntry(
                    uniqueIdentifier = clazz.fullQualifiedName,
                    className = clazz.className,
                    classAlias = clazz.fullQualifiedName.replace(".", "_").trim('_'),
                    attributes = clazz.getAllProperties().filter { options?.isValid(it, logger) == true }.map { it.toAttribute() }.toList(),
                    functions = clazz.getAllFunctions().filter { options?.isValid(it, logger) == true }.map { it.toFunction() }.toList()
                )
            } else {
                null
            }
        }
    }
}
