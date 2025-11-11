public interface Server {
    fun start(): Unit
    fun stop(): Unit
}

public abstract class AbstractServer() : Server {
    override fun start(): Unit {}
    override fun stop(): Unit {}
    fun handleRequest(): Unit {}
    fun sendResponse(): Unit {}
}

internal class ServerImpl : AbstractServer {
    override fun sendResponse(): Unit {}
}
