package com.animals.pets

internal class Dog : Pet() {
    override val name: String = "DOG"
    override fun makeSound(): String {
        return "Wuff"
    }
}