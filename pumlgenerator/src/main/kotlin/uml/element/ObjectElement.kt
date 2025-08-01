package uml.element

import Options
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import filterFunctionsByOptions
import filterPropertiesByOptions
import isValid
import uml.DiagramElement
import uml.ElementKind
import uml.className
import uml.fullQualifiedName

class ObjectElement(
    uniqueIdentifier: String,
    elementName: String,
    elementAlias: String,
    attributes: List<Field>,
    functions: List<Method>,
    isShell: Boolean
) : AbstractElement(elementName, elementAlias, uniqueIdentifier, attributes, functions, isShell) {

    override val elementKind: ElementKind = ElementKind.OBJECT

    class Builder(clazz: KSClassDeclaration, isShell: Boolean, options: Options, logger: KSPLogger?) : AbstractElementBuilder(clazz, isShell, options, logger) {

        override fun build(): ObjectElement? {
            return if (options.isValid(clazz, logger)) {
                ObjectElement(
                    uniqueIdentifier = clazz.fullQualifiedName,
                    elementName = clazz.className,
                    elementAlias = clazz.fullQualifiedName.replace(".", "_").trim('_'),
                    attributes = allProperties.map { it.toField(options) },
                    functions = allFunctions.map { it.toMethod(options) },
                    isShell = isShell
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
