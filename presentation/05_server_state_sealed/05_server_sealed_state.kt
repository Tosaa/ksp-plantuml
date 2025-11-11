public interface Server {
    fun start(): Unit
    fun stop(): Unit
    val state: State

    sealed class State() {
        object STARTED : State()
        object STARTING : State()
        class STOPPING(val reason: String) : State()
        class STOPPED(val reason: String) : State()
    }
}