package com.city

/**
 * Represents a sensor attached to a device.
 */
class Sensor(val type: String, val device: Device) : Operable {

    /**
     * Operates the sensor and returns its status.
     */
    override fun operate(): Status = device.status

    /**
     * Generates a report from this sensor.
     */
    fun generateReport(): Report = Report("Sensor $type status: ${device.status}", null, this)
}
