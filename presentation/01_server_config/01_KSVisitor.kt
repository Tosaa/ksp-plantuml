class MyKSVisitor() : KSVisitorVoid() {

    override fun visitFile(
        file: KSFile,
        data: Unit
    ) {
        file.declarations.forEach { declaration ->
            declaration.accept(this, Unit)
        }
    }

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: Unit
    ) {
        classDeclaration.getDeclaredFunctions().forEach { function ->
            analyseFunction(function)
        }
        classDeclaration.getDeclaredProperties().forEach { property ->
            analyseProperty(property)
        }

        // This is what I missed first
        classDeclaration.declarations
            .filterNot { it in classDeclaration.getDeclaredProperties() }
            .filterNot { it in classDeclaration.getDeclaredFunctions() }
            .filterIsInstance<KSClassDeclaration>()
            .forEach { declaration ->
                declaration.accept(this, Unit)
            }
    }

    private fun analyseFunction(function: KSFunctionDeclaration) {}
    private fun analyseProperty(function: KSPropertyDeclaration) {}
}
