package com.city

/**
 * Represents different types of alerts that can occur in the SmartCity.
 */
sealed class Alert {
    /**
     * Fire alert with location.
     */
    data class Fire(val location: String) : Alert()

    /**
     * Flood alert with severity level.
     */
    data class Flood(val severity: Int) : Alert()

    /**
     * Power outage alert.
     */
    object PowerOutage : Alert()
}
