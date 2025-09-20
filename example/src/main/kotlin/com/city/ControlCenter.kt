package com.city

/**
 * Represents the control center managing reports and traffic lights.
 */
class ControlCenter(val reports: List<Report>, val trafficLights: List<TrafficLight>) {

    /**
     * Logs a new report.
     */
    fun logReport(report: Report): List<Report> = reports + report
}
