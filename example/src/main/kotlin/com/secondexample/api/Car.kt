package com.secondexample.api

interface Car {
    val hexColor: String
    val wheels: Int
    val doors: Int
    val isElectric: Boolean
    val identificationNumber: ID

    interface ID {
        val id: String
    }
}