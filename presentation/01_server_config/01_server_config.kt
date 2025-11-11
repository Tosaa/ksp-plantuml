public interface Server {
	fun start(): Unit
    fun stop(): Unit

    interface Config {
        val ipAddress: String
        val port: Int
    }
}
