package uml.element

import Options
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import isValid
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
    isShell: Boolean
) : DiagramElement(elementName, elementAlias, uniqueIdentifier, attributes, functions, isShell) {

    override val elementKind: ElementKind = ElementKind.INTERFACE(isSealedClass)

    class Builder(clazz: KSClassDeclaration, isShell: Boolean, options: Options, logger: KSPLogger?) : DiagramElementBuilder(clazz, isShell, options, logger) {

        override fun build(): InterfaceElement? {
            return if (options.isValid(clazz, logger)) {
                InterfaceElement(
                    uniqueIdentifier = clazz.fullQualifiedName,
                    elementName = clazz.className,
                    elementAlias = clazz.fullQualifiedName.replace(".", "_").trim('_'),
                    attributes = allProperties.map { it.toField(options) },
                    functions = allFunctions.map { it.toMethod(options) },
                    isSealedClass = clazz.modifiers.contains(Modifier.SEALED),
                    isShell = isShell
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
