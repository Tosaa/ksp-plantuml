package uml.element

import Notation
import Options
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import isValid
import toPumlAlias
import uml.ElementKind
import uml.className
import uml.fullQualifiedName

class InterfaceElement(
    uniqueIdentifier: String,
    elementName: String,
    elementAlias: String,
    attributes: List<Field>,
    functions: List<Method>,
    isSealedClass: Boolean,
    isShell: Boolean,
    notation: Notation
) : DiagramElement(elementName, elementAlias, uniqueIdentifier, attributes, functions, isShell, notation) {

    override val elementKind: ElementKind = ElementKind.INTERFACE(isSealedClass)

    class Builder(clazz: KSClassDeclaration, isShell: Boolean, options: Options, logger: KSPLogger?) : DiagramElementBuilder(clazz, isShell, options, logger) {

        override fun buildElement(): InterfaceElement? {
            return if (options.isValid(clazz, logger)) {
                InterfaceElement(
                    uniqueIdentifier = clazz.fullQualifiedName,
                    elementName = clazz.className,
                    elementAlias = clazz.fullQualifiedName.toPumlAlias(),
                    attributes = allProperties,
                    functions = allFunctions,
                    isSealedClass = clazz.modifiers.contains(Modifier.SEALED),
                    isShell = isShell,
                    notation = options.notation
                )
            } else {
                null
            }
        }

        override fun toString(): String {
            return "InterfaceBuilder(clazz=${clazz.fullQualifiedName})"
        }
    }
}
