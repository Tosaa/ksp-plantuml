package uml

import com.google.devtools.ksp.symbol.KSClassDeclaration

val KSClassDeclaration.fullQualifiedName: String
    get() = "${qualifiedName?.getQualifier() ?: packageName.asString()}.${simpleName.asString()}"

val KSClassDeclaration.className: String
    get() = fullQualifiedName.replace(packageName.asString(), "").trim('.')
