
fun KSFunctionDeclaration.isInherited(): Boolean {
    if (this.modifiers.contains(Modifier.OVERRIDE)) {
        return true
    }
    val ownerOfThisFunction = this.parentDeclaration as? KSClassDeclaration
    val parents: List<KSClassDeclaration> = ownerOfThisFunction
        ?.superTypes
        ?.toList()
        ?.map { it.resolve().declaration }
        ?.filterIsInstance<KSClassDeclaration>() ?: emptyList()
    val parentFunctions: List<String> = parents
        .flatMap { it.getAllFunctions().toList() }
        .mapNotNull { it.qualifiedName?.asString() }
    if (this.qualifiedName?.asString() in parentFunctions) {
        return true
    }
    // ... more checks
    return true
}
