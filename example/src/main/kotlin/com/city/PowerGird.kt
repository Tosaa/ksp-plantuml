package com.city

/**
 * Represents the power grid system.
 */
class PowerGrid(val gridId: String, val sensors: List<Sensor>, val controlCenter: ControlCenter) {

    /**
     * Checks all sensors in the grid.
     */
    fun checkSensors(): List<Report> = sensors.map { it.generateReport() }
}
