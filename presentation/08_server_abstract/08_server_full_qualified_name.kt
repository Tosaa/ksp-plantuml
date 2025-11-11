// fullqualifiedName = Server
public interface Server {
// fullqualifiedName = Server.start
    fun start(): Unit
// fullqualifiedName = Server.stop
    fun stop(): Unit
}

// fullqualifiedName = AbstractServer
public abstract class AbstractServer() : Server {
    // fullqualifiedName = AbstractServer.start
    override fun start(): Unit {}
    // fullqualifiedName = AbstractServer.stop
    override fun stop(): Unit {}
    // fullqualifiedName = AbstractServer.handleRequest
    fun handleRequest(): Unit {}
    // fullqualifiedName = AbstractServer.sendResponse
    fun sendResponse(): Unit {}
}

// fullqualifiedName = ServerImpl
internal class ServerImpl : AbstractServer {
    // fullqualifiedName = ServerImpl.sendResponse
    override fun sendResponse(): Unit {}
    // fullqualifiedName = AbstractServer.start
    // fullqualifiedName = AbstractServer.stop
    // fullqualifiedName = AbstractServer.handleRequest
}
