package com.firstexample.first

import com.firstexample.second.Bar


class Foo(val bar: Bar) {
    companion object {
        fun barToFoo(bar: Bar): Foo = Foo(bar)
        fun test(): Unit = Unit
        fun test2(): Unit = Unit
    }
}
