package com.animals.pets

import com.animals.Animal

abstract class Pet: Animal {
    override val isExtinct: Boolean = false
    override fun makeSound(): String = ""
}