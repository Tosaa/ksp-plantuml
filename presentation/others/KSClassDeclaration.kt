interface KSClassDeclaration : KSDeclaration, KSDeclarationContainer {

    val classKind: ClassKind

    val primaryConstructor: KSFunctionDeclaration?

    val superTypes: Sequence<KSTypeReference>

    val isCompanionObject: Boolean

    fun getSealedSubclasses(): Sequence<KSClassDeclaration>

    fun getAllFunctions(): Sequence<KSFunctionDeclaration>

    fun getAllProperties(): Sequence<KSPropertyDeclaration>

    fun asType(typeArguments: List<KSTypeArgument>): KSType

    fun asStarProjectedType(): KSType
}