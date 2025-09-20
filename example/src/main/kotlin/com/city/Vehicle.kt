import com.city.MaintenanceCrew

/**
 * Represents a vehicle assigned to a maintenance crew.
 */
class Vehicle(val licensePlate: String, val assignedCrew: MaintenanceCrew) {

    /**
     * Dispatches the vehicle to a location.
     */
    fun dispatch(location: String): String = "Vehicle $licensePlate dispatched to $location"
}
