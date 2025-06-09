package first

import second.Bar

open class OuterClass {
    open val foo: Foo? = null

    class InnerClass {
        val bar: Bar = Bar()
    }

    class InnerInheritedOuterClass() : OuterClass() {
        override val foo = Foo(Bar())
    }

}