import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSNode
import java.io.File

class KSPWithFileLogger(val kspLogger: KSPLogger, private val file: File? = null) : KSPLogger {
    override fun error(message: String, symbol: KSNode?) {
        file?.apply {
            appendText("E ")
            appendText(message)
            appendText("\n")
        }
        kspLogger.error(message, symbol)
    }

    override fun exception(e: Throwable) {
        file?.apply {
            appendText(e.stackTraceToString().lines().map { it.ensureStartsWith("E ") }.joinToString("\n"))
            appendText("\n")
        }
        kspLogger.exception(e)
    }

    override fun info(message: String, symbol: KSNode?) {
        file?.apply {
            appendText("I ")
            appendText(message)
            appendText("\n")
        }
        kspLogger.info(message, symbol)
    }

    override fun logging(message: String, symbol: KSNode?) {
        file?.apply {
            appendText("L ")
            appendText(message)
            appendText("\n")
        }
        kspLogger.logging(message, symbol)
    }

    override fun warn(message: String, symbol: KSNode?) {
        file?.apply {
            appendText("W ")
            appendText(message)
            appendText("\n")
        }
        kspLogger.warn(message, symbol)
    }

}

fun KSPLogger?.i(node: KSNode? = null, message: () -> String) {
    this?.info(message(), node)
}

fun KSPLogger?.v(node: KSNode? = null, message: () -> String) {
// Using logging() when --debug is used did not work->use Info instead
// this.logging(message(), node)
    this?.logging(message(), node)
}

fun KSPLogger?.w(node: KSNode? = null, message: () -> String) {
    this?.warn(message(), node)
}

// Logging with this function will cause the Gradle task to fail!
fun KSPLogger?.e(node: KSNode? = null, message: () -> String) {
    this?.error(message(), node)
}