package uml.element

import Options
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import isValid
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
    isShell: Boolean
) : DiagramElement(elementName, elementAlias, uniqueIdentifier, attributes, functions, isShell) {

    override val elementKind: ElementKind = ElementKind.CLAZZ(isSealed = isSealedClass, isData = isDataClass)

    class Builder(clazz: KSClassDeclaration, isShell: Boolean, options: Options, logger: KSPLogger? = null) : DiagramElementBuilder(clazz, isShell, options, logger) {

        override fun build(): ClassElement? {
            return if (options.isValid(clazz, logger)) {
                ClassElement(
                    uniqueIdentifier = clazz.fullQualifiedName,
                    elementName = clazz.className,
                    elementAlias = clazz.fullQualifiedName.replace(".", "_").trim('_'),
                    attributes = allProperties.map { it.toField(options) },
                    functions = allFunctions.map { it.toMethod(options) },
                    isSealedClass = clazz.modifiers.contains(Modifier.SEALED),
                    isDataClass = clazz.modifiers.contains(Modifier.DATA),
                    isShell = isShell
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
