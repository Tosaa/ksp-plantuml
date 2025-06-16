package com.secondexample.car

import com.secondexample.api.Car
import kotlin.random.Random

class CarID private constructor(override val id: String) : Car.ID {
    companion object {
        const val maxLetters = 6
        fun createRandomCarID(seed: Int): CarID {
            return CarID((0..<6).map { Random(seed).nextInt('A'.code, 'Z'.code).toChar() }.joinToString())
        }
    }
}