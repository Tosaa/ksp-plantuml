/**
 * A declaration container can have a list
 * of declarations declared in it.
 */
interface KSDeclarationContainer : KSNode {
    /**
     * Declarations that are lexically
     * declared inside the current container.
     */
    val declarations: Sequence<KSDeclaration>
}