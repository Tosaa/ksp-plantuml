package com.city

import Vehicle

/**
 * Represents emergency services in a district.
 */
class EmergencyService(val vehicles: List<Vehicle>, val alerts: List<Alert>) {

    /**
     * Responds to all active alerts.
     */
    fun respondToAlerts(): List<String> = alerts.map { alert ->
        "Responding to alert: $alert"
    }
}
