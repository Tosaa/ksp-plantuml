public interface Server {
	fun start(): Unit
    fun stop(): Unit
}

internal class ServerImpl : Server {
    override fun start(): Unit{}    
    override fun stop(): Unit{}
    fun handleRequest(): Unit{}
    fun sendResponse(): Unit{}
}
