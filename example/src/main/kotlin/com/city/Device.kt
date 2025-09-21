package com.city

/**
 * Represents a device with an operational status.
 */
class Device(val id: String, val status: Status) {

    /**
     * Toggles the device status.
     */
    fun toggleStatus(): Status = if (status == Status.ACTIVE) Status.INACTIVE else Status.ACTIVE
}
