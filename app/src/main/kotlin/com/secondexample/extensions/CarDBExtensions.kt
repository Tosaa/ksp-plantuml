package com.secondexample.extensions

import com.secondexample.car.CarID
import com.secondexample.persistency.CarDB

private fun CarID.isNew():Boolean{
    return this.id.startsWith("5")
}

suspend fun CarDB.retrieveOnlyNewCarIDs(): List<CarID>{
    return this.retrieveSavedCarIDs().filter { it.isNew() }
}