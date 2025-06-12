package uml.component

import Options
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import isValid
import uml.className
import uml.component.DiagramComponent.Companion.FUNCTION_INDENT
import uml.component.DiagramComponent.Companion.PROPERTY_INDENT
import uml.fullQualifiedName
import kotlin.math.log


data class EnumEntry(val uniqueIdentifier: String, val enumName: String, val enumAlias: String, override val attributes: List<ClassAttribute>, val functions: List<ClassFunction>, val members: List<String>) : DiagramComponent {
    override fun render(): String {
        val membersString = members
            .takeIf { it.isNotEmpty() }
            ?.joinToString("\n", prefix = "\n", postfix = "\n${PROPERTY_INDENT}__") { "$PROPERTY_INDENT$it" }
            ?: ""
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
    enum "$enumName" as $enumAlias{$membersString$attributesString$functionsString
    }
"""
    }

    class Builder(val clazz: KSClassDeclaration, override val options: Options? = null, val logger: KSPLogger?) : DiagramComponent.Builder<EnumEntry> {
        override fun build(): EnumEntry? {
            return if (options.isValid(clazz, logger)) {
                EnumEntry(
                    uniqueIdentifier = clazz.fullQualifiedName,
                    enumName = clazz.className,
                    enumAlias = clazz.fullQualifiedName.replace(".", "_").trim('_'),
                    attributes = clazz.getAllProperties().filter { options?.isValid(it, logger) == true }.map { it.toAttribute() }.toList(),
                    functions = clazz.getAllFunctions().filter { options?.isValid(it, logger) == true }.map { it.toFunction() }.toList(),
                    members = clazz.declarations
                        .mapNotNull { it as? KSClassDeclaration }
                        .filter { it.classKind == ClassKind.ENUM_ENTRY }
                        .map { it.simpleName.asString() }
                        .toList()
                )
            } else {
                null
            }
        }
    }
}
