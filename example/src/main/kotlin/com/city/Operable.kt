package com.city

/**
 * Interface for components that can be operated.
 */
interface Operable {
    /**
     * Operates the component and returns its current status.
     */
    fun operate(): Status
}
