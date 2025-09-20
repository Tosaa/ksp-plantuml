package com.city

/**
 * Represents a district within a city.
 */
class District(val name: String, val buildings: List<Building>, val emergencyService: EmergencyService) {

    /**
     * Generates a report for the district.
     */
    fun generateDistrictReport(): List<Report> = buildings.flatMap { it.inspectBuilding() }
}
