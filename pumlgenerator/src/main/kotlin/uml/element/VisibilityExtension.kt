package uml.element

import com.google.devtools.ksp.symbol.Visibility

val Visibility.pumlVisibility: String
    get() = when(this){
        Visibility.PUBLIC -> "+"
        Visibility.PRIVATE -> "-"
        Visibility.PROTECTED -> "#"
        Visibility.INTERNAL -> "#"
        Visibility.LOCAL -> "#"
        Visibility.JAVA_PACKAGE -> "#"
    }