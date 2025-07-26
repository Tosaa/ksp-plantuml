package com.animals.research

import com.animals.Animal

internal class TRex : Animal {
    override val name: String = "T-REX"
    override val isExtinct: Boolean = true
    override fun makeSound(): String {
        return "Raaaarrrrr"
    }
}