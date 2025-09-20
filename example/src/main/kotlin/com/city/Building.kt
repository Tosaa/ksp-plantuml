package com.city


/**
 * Represents a building with sensors and citizens.
 */
class Building(val id: Int, val sensors: List<Sensor>, val citizens: List<Citizen>) {

    /**
     * Inspects the building and returns sensor-generated reports.
     */
    fun inspectBuilding(): List<Report> = sensors.map { sensor ->
        Report("Sensor ${sensor.type} checked", null, sensor)
    }
}
