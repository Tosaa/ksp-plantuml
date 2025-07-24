package com.animals

abstract class AbstractAnimal {
    abstract val name: String
    val isExtinct: Boolean = false
    open fun makeSound(): String = ""
}