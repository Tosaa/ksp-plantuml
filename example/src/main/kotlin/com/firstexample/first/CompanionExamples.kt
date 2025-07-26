package com.firstexample.first

interface Example {
    val number: Int
}

class CompanionExamples {

    companion object : Example {
        override val number: Int
            get() = 0
    }
}