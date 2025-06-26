package com.secondexample.extensions

import com.secondexample.api.Car
import com.secondexample.persistency.CarDB

private fun Car.ID.isNew():Boolean{
    return this.id.startsWith("5")
}

suspend fun CarDB.retrieveOnlyNewCarIDs(): List<Car.ID>{
    return this.retrieveSavedCarIDs().filter { it.isNew() }
}