package uml

import Notation
import com.google.devtools.ksp.symbol.Visibility

fun Visibility.pumlVisibility(notation: Notation): String = when (this) {
    Visibility.PUBLIC -> "+"
    Visibility.PRIVATE -> "-"
    Visibility.PROTECTED -> "#"
    // UML has no visibility for a module, the visibility for a package is the closest one.
    // The notation of plantuml keeps the one of the earlier versions.
    Visibility.INTERNAL -> if (notation == Notation.UML) "~" else "#"
    Visibility.LOCAL -> "#"
    Visibility.JAVA_PACKAGE -> if (notation == Notation.UML) "~" else "#"
}
