/**
 * A visitor for program elements
 */
interface KSVisitor<D, R> {
    fun visitFile(file: KSFile, data: D): R

    fun visitFunctionDeclaration(
        function: KSFunctionDeclaration,
        data: D
    ): R

    fun visitPropertyDeclaration(
        property: KSPropertyDeclaration,
        data: D
    ): R

    fun visitTypeAlias(
        typeAlias: KSTypeAlias,
        data: D
    ): R

    fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: D
    ): R
}