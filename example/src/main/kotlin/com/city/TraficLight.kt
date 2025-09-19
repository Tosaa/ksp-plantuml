package com.city

/**
 * Represents a traffic light controlled by the control center.
 */
class TrafficLight(val location: String, val controlCenter: ControlCenter) : Operable {

    /**
     * Operates the traffic light.
     */
    override fun operate(): Status = Status.ACTIVE

    /**
     * Changes the light color.
     */
    fun changeLight(color: String): String = "Traffic light at $location changed to $color"
}
