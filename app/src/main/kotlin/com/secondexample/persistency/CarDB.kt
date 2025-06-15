package com.secondexample.persistency

import com.secondexample.car.CarID

interface CarDB {
    suspend fun saveCarID(carID: CarID)

    suspend fun retrieveSavedCarIDs(): List<CarID>
}