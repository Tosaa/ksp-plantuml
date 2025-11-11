public interface Server {
    fun start(): Unit
    fun stop(): Unit
    val state: State

    enum class State {
        STARTED,
        STARTING,
        STOPPING,
        STOPPED
    }
}