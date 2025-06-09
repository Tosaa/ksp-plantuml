import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSNode

fun KSPLogger?.i(node: KSNode? = null, message: () -> String) {
    this?.info(message(), node)
}

fun KSPLogger?.v(node: KSNode? = null, message: () -> String) {
// Using logging() when --debug is used did not work->use Info instead
// this.logging(message(), node)
    this?.info(message(), node)
}

fun KSPLogger?.w(node: KSNode? = null, message: () -> String) {
    this?.warn(message(), node)
}