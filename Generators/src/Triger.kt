import kotlin.reflect.KClass

class Trigger<T : Trigger.TriggerTable>(
    internal val time: TrigerTimes,
    internal val event: TrigerEvents,
    internal val table: KClass<T>,
    name: String,
    delimiter: String = "!!",
    nameGenerator: NameGenerator = DefaultNameGenerator(),
    internal val func: Trigger<T>.(table: T) -> Unit
) : BaseComponent(name, delimiter, nameGenerator) {
    abstract class TriggerTable {
        internal fun getName(): String = this::class.simpleName.toString()
    }

    override fun callFunc() = func(tableInstance)

    internal fun generateTableInstance(): T {
        table.constructors.first().let { constructor ->
            constructor.parameters.map { it to it.name }.toMap().let {
                return constructor.callBy(it)
            }
        }
    }

    internal val tableInstance: T

    init {
        tableInstance = generateTableInstance()
    }

    override fun generateScript(): String = generateInnerScript().let {
        "delimiter $delimiter\n" +
                "drop trigger if exists $name !!\n" +
                "create trigger $name\n" +
                "$time $event\n" +
                "on ${tableInstance.getName()}\n" +
                "for each row\n" +
                "begin\n" +
                it.body +
                "\nend $delimiter\n" +
                "delimiter ;"
    }

}

enum class TrigerTimes {
    Before {
        override fun toString(): String = super.toString().toLowerCase()
    },
    After {
        override fun toString(): String = super.toString().toLowerCase()
    }
}

enum class TrigerEvents {
    Insert {
        override fun toString(): String = super.toString().toLowerCase()
    },
    Update {
        override fun toString(): String = super.toString().toLowerCase()
    },
    Delete {
        override fun toString(): String = super.toString().toLowerCase()
    }
}