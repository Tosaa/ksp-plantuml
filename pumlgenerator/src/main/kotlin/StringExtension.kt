fun String.ensureStartsWith(char: Char): String = if (this.startsWith(char)) this else char + this
fun String.ensureStartsWith(string: String): String = if (this.startsWith(string)) this else string + this
fun String.ensureEndsWith(string: String): String = if (this.endsWith(string)) this else this + string

/**
 * Converts a full qualified name to the alias used to identify the element inside the plantuml file,
 * e.g. `com.example.Foo` becomes `com_example_Foo`.
 */
fun String.toPumlAlias(): String = replace(".", "_").trim('_')
