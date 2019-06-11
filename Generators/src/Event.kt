class Event(
    name: String,
    internal val eventTimes: EventTimes,
    internal val time: String,
    internal val preserve: Boolean = true,
    delimiter: String = "!!",
    nameGenerator: NameGenerator = DefaultNameGenerator(),
    internal val func: Event.() -> Unit
) :
    BaseComponent(name, delimiter, nameGenerator) {
    override fun callFunc() = func()

    override fun generateScript(): String = generateInnerScript().let {
        "delimiter $delimiter\n" +
                "drop event if exists $name !!\n" +
                "create event $name on schedule $eventTimes $time ${if (preserve) "on completion preserve" else ""} do\n" +
                "begin\n" +
                it.body +
                "\nend $delimiter\n" +
                "delimiter ;"
    }
}

enum class EventTimes {
    At {
        override fun toString(): String = super.toString().toLowerCase()
    },
    Every {
        override fun toString(): String = super.toString().toLowerCase()
    }
}