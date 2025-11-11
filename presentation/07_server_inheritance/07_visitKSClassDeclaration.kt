override fun visitClassDeclaration(
    classDeclaration: KSClassDeclaration,
    data: Unit
) {
    val functions = classDeclaration
        .getAllFunctions()
        .filterNot { it.isInhertied() }

    val properties = classDeclaration
        .getAllProperties()
        .filterNot { it.isInhertied() }
}