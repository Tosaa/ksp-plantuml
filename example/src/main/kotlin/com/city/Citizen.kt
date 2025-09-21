package com.city

/**
 * Represents a citizen living in a building.
 */
class Citizen(val name: String, val home: Building) {

    /**
     * Reports an issue to the control center.
     */
    fun reportIssue(issue: String): Alert = Alert.Fire("Reported by $name: $issue")
}
