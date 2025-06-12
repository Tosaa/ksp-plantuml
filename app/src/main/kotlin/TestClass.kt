import com.firstexample.first.Foo
import com.firstexample.second.Test

class TestClass(val a: Boolean) : Test {
    val b: String = ""
    val d: Foo? = null
    val c by lazy { 42 }

    override fun isValid(): Boolean {
        return false
    }
}