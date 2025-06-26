package uml.element

import Options
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import isValid
import uml.DiagramElement
import uml.ElementKind
import uml.className
import uml.fullQualifiedName

data class ObjectElement(
    val uniqueIdentifier: String,
    override val elementName: String,
    override val elementAlias: String,
    val attributes: List<Field>,
    val functions: List<Method>
) : DiagramElement() {
    override val comment: String = "'$uniqueIdentifier"
    override val elementKind: ElementKind = ElementKind.OBJECT
    override fun getContent(indent: String): String {
        val attributesString = attributes
            .takeIf { it.isNotEmpty() }
            ?.let { it.joinToString(separator = "\n") { "$indent${it.render()}" } }
            ?: ""
        val functionsString = functions
            .takeIf { it.isNotEmpty() }
            ?.let { it.joinToString(separator = "\n") { "$indent${it.render()}" } }
            ?: ""
        return """
$attributesString
$functionsString
"""
    }

    class Builder(override val clazz: KSClassDeclaration, override val options: Options? = null, val logger: KSPLogger?) : DiagramElement.Builder<ObjectElement> {
        val companionObject = clazz.declarations.filter { it is KSClassDeclaration && it.isCompanionObject }.map { it as? KSClassDeclaration }.firstOrNull()

        override val properties: MutableList<KSPropertyDeclaration> = mutableListOf<KSPropertyDeclaration>().apply {
            addAll((companionObject?.getAllProperties() ?: emptySequence()).filter { options?.isValid(it, logger) == true })
            addAll(clazz.getAllProperties().filter { options?.isValid(it, logger) == true })
        }

        override val functions: MutableList<KSFunctionDeclaration> = mutableListOf<KSFunctionDeclaration>().apply {
            addAll((companionObject?.getAllFunctions() ?: emptySequence()).filter { options?.isValid(it, logger) == true })
            addAll(clazz.getAllFunctions().filter { options?.isValid(it, logger) == true })
        }

        override fun build(): ObjectElement? {
            return if (options.isValid(clazz, logger)) {
                ObjectElement(
                    uniqueIdentifier = clazz.fullQualifiedName,
                    elementName = clazz.className,
                    elementAlias = clazz.fullQualifiedName.replace(".", "_").trim('_'),
                    attributes = properties.map { it.toField(options) },
                    functions = functions.map { it.toMethod(options) },
                )
            } else {
                null
            }
        }
        
        override fun toString(): String {
            return "ObjectBuilder(clazz=${clazz.fullQualifiedName})"
        }
    }
}
