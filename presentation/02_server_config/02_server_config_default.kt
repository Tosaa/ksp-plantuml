public interface Server {
    fun start(): Unit
    fun stop(): Unit

    interface Config {
        val ipAddress: String
        val port: Int

        companion object {
            val Default: Config = object : Config {
                override val ipAddress: String = "127.0.0.1"
                override val port: Int = 8080
            }
        }
    }
}