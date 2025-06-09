import first.Foo
import second.Test

class TestClass(val a: Boolean) : Test {
    val b: String = ""
    val c by lazy { 42 }
    val d: Foo? = null

    override fun isValid(): Boolean {
        return false
    }
}