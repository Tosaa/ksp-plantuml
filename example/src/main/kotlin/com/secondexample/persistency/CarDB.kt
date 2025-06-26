package com.secondexample.persistency

import com.secondexample.api.Car

interface CarDB {
    suspend fun saveCarID(carID: Car.ID)

    suspend fun retrieveSavedCarIDs(): List<Car.ID>
}