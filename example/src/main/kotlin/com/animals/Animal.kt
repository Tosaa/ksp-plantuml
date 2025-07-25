package com.animals

public interface Animal {
    val name: String
    val isExtinct: Boolean
    fun makeSound(): String
}