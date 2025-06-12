package com.firstexample.first

interface OuterInterface {
    fun a(): Unit

    interface InnerInterface : OuterInterface {
        fun b(): Unit
    }
}