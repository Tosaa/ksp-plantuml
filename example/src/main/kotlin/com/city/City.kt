package com.city

/**
 * Represents a city containing multiple districts and a control center.
 */
class City(val name: String, val districts: List<District>, val controlCenter: ControlCenter) {

    /**
     * Generates a city-wide report.
     */
    fun generateCityReport(): List<Report> = districts.flatMap { it.generateDistrictReport() }
}
