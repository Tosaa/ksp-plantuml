package com.secondexample.factory

import com.secondexample.api.Car
import com.secondexample.api.CarFactory
import com.secondexample.car.CarID
import com.secondexample.car.CarImpl
import kotlin.random.Random

class CarFactoryImpl(val hexColor: String) : CarFactory {
    override fun create(): Car {
        return CarImpl(
            hexColor, 4, 5, true, identificationNumber = CarID.createRandomCarID(Random.nextInt())
        )
    }

}