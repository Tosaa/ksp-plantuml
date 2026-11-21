package uml.element

import Options
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import isValid
import toPumlAlias
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
) : DiagramElement(elementName, elementAlias, uniqueIdentifier, attributes, functions, isShell) {

    override val elementKind: ElementKind = ElementKind.OBJECT

    class Builder(clazz: KSClassDeclaration, isShell: Boolean, options: Options, logger: KSPLogger?) : DiagramElementBuilder(clazz, isShell, options, logger) {

        override fun buildElement(): ObjectElement? {
            return if (options.isValid(clazz, logger)) {
                ObjectElement(
                    uniqueIdentifier = clazz.fullQualifiedName,
                    elementName = clazz.className,
                    elementAlias = clazz.fullQualifiedName.toPumlAlias(),
                    attributes = allProperties,
                    functions = allFunctions,
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
