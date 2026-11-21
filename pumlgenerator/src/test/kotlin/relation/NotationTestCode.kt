package relation

/**
 * Code with the constructs that are drawn differently by the notations, see [UmlNotationTest] and [PlantumlNotationTest].
 */
object NotationTestCode {

    val shapes = """
    package shapes
    interface Shape {
        fun area(): Double
    }
    interface Polygon : Shape {
        val sides: Int
    }
    class Square(override val sides: Int) : Polygon {
        override fun area(): Double = 0.0
    }
    object UnitCircle : Shape {
        override fun area(): Double = 3.14
    }
    """

    val rooms = """
    interface Room {
        fun connect(room: Room?)
    }
    class MagicRoom : Room {
        override fun connect(room: Room?) {}
    }
    """

    val car = """
    package city
    class Engine
    class Radio
    class Wheel
    class Seat
    class Driver
    class Horn
    class Car {
        val engine: Engine = Engine()
        val radio: Radio? = null
        val wheels: List<Wheel> = emptyList()
        val seats: Array<Seat> = emptyArray()
        val lastDriver: Result<Driver> = Result.success(Driver())
        fun horn(): Horn = Horn()
    }
    """

    val listeners = """
    package listeners
    class Event
    class Batch
    interface EventListener {
        fun onEvent(event: Event)
        fun onBatches(batches: List<Batch>)
    }
    """

    val family = """
    package family
    class Parent {
        val children: List<Child> = emptyList()
    }
    class Child(val parent: Parent)
    """

    val members = """
    package members
    enum class Level { LOW, HIGH }
    annotation class Experimental
    interface Sensor {
        suspend fun read(level: Level, retries: Int): Int
        fun log(vararg lines: String)
    }
    open class Device {
        val name: String = ""
        internal val id: Int = 0
        protected val secret: Int = 0
        private val hidden: Int = 0
    }
    fun Device.reset(force: Boolean) {}
    val Device.label: String
        get() = name
    """

    // Members without a visibility modifier are package private in Java
    val membersJava = """
    package members;
    public class Valve {
        int pressure;
        void open() {}
    }
    """
}
