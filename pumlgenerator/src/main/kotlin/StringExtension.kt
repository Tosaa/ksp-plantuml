fun String.ensureStartsWith(char: Char): String = if (this.startsWith(char)) this else char + this
fun String.ensureStartsWith(string: String): String = if (this.startsWith(string)) this else string + this
fun String.ensureEndsWith(string: String): String = if (this.endsWith(string)) this else this + string
