package com.city

/**
 * Represents a weather station with sensors.
 */
class WeatherStation(val location: String, val sensors: List<Sensor>) : Operable {

    /**
     * Operates the weather station.
     */
    override fun operate(): Status = Status.ACTIVE

    /**
     * Collects weather data.
     */
    fun collectData(): List<Report> = sensors.map { it.generateReport() }
}
