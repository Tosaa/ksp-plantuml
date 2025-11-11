/**
 * A type alias
 */
interface KSTypeAlias : KSDeclaration {
    /**
     * The name of the type alias
     */
    val name: KSName

    /**
     * The reference to the aliased type.
     */
    val type: KSTypeReference
}