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

class ClassElement(
    elementName: String,
    elementAlias: String,
    uniqueIdentifier: String,
    attributes: List<Field>,
    functions: List<Method>,
    val isSealedClass: Boolean,
    val isDataClass: Boolean,
    isShell: Boolean,
    notation: Notation
) : DiagramElement(elementName, elementAlias, uniqueIdentifier, attributes, functions, isShell, notation) {

    override val elementKind: ElementKind = ElementKind.CLAZZ(isSealed = isSealedClass, isData = isDataClass)

    class Builder(clazz: KSClassDeclaration, isShell: Boolean, options: Options, logger: KSPLogger? = null) : DiagramElementBuilder(clazz, isShell, options, logger) {

        override fun buildElement(): ClassElement? {
            return if (options.isValid(clazz, logger)) {
                ClassElement(
                    uniqueIdentifier = clazz.fullQualifiedName,
                    elementName = clazz.className,
                    elementAlias = clazz.fullQualifiedName.toPumlAlias(),
                    attributes = allProperties,
                    functions = allFunctions,
                    isSealedClass = clazz.modifiers.contains(Modifier.SEALED),
                    isDataClass = clazz.modifiers.contains(Modifier.DATA),
                    isShell = isShell,
                    notation = options.notation
                )
            } else {
                null
            }
        }

        override fun toString(): String {
            return "ClassBuilder(clazz=${clazz.fullQualifiedName})"
        }

    }
}
