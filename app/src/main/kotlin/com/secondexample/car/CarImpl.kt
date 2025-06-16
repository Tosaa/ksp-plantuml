package com.secondexample.car

import com.secondexample.api.Car

class CarImpl(override val hexColor: String, override val wheels: Int, override val doors: Int, override val isElectric: Boolean, override val identificationNumber: Car.ID) : Car {
}