package first

import second.Bar


class Foo(val bar: Bar) {
    companion object {
        fun barToFoo(bar: Bar): Foo = Foo(bar)
        fun test(): Unit = Unit
    }
}
