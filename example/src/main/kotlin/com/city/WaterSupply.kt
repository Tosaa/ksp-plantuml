package com.city

/**
 * Represents the water supply system.
 */
class WaterSupply(val source: String, val buildings: List<Building>) {

    /**
     * Distributes water to all buildings.
     */
    fun distributeWater(): String = "Water distributed from $source to ${buildings.size} buildings"
}
