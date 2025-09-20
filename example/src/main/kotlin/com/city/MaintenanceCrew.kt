package com.city

/**
 * Represents a maintenance crew assigned to a district.
 */
class MaintenanceCrew(val members: List<Citizen>, val assignedDistrict: District) {

    /**
     * Performs maintenance in the district.
     */
    fun performMaintenance(): String = "Maintenance performed in ${assignedDistrict.name}"
}
