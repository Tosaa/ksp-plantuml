package first

interface OuterInterface {
    fun a(): Unit

    interface InnerInterface : OuterInterface {
        fun b(): Unit
    }
}