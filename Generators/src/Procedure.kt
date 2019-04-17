import ReturnTypes.*
import kotlin.reflect.KClass

open class Procedure(
    internal val name: String,
    internal val delimiter: String = "!!",
    internal val nameGenerator: NameGenerator = DefaultNameGenerator(),
    internal val func: (procedure: Procedure) -> Unit = {}
) {
    internal val commands = mutableListOf<ReturnTypes.generic>()

    open fun generateScript(): String {
        func(this)
        commands.sortBy {
            when (it) {
                is Parameter -> 0
                is Variable -> 1
                is Cursor<*> -> 2
                is Handler -> 3
                is ReturnTypes.Function -> 4
                else -> it.priority
            }
        }

        return commands.filter { it is Parameter }.joinToString(
            ",\n",
            "delimiter $delimiter\ncreate procedure $name(\n",
            "\n) begin\n"
        ) { it.generateScript() } +
                commands.filter { it !is Parameter }.joinToString(
                    "\n",
                    postfix = "\nend $delimiter\ndelimiter ;"
                ) { it.generateScript() }
    }

    fun parameter(type: DataTypes.generic, name: String = ""): Parameter {
        (if (name.isBlank()) nameGenerator.getNext() else name).let { name ->
            Parameter(name, type.toString()).let {
                commands.add(it)
                return it
            }
        }
    }

    fun variable(type: DataTypes.generic, name: String = ""): Variable {
        (if (name.isBlank()) nameGenerator.getNext() else name).let { name ->
            Variable(name, type.toString()).let {
                commands.add(it)
                return it
            }
        }
    }

    fun <T : Any> list(query: Sql, expectedResult: KClass<T>, name: String = ""): Cursor<T> {
        (if (name.isBlank()) nameGenerator.getNext() else name).let { name ->
            Cursor(name, query, expectedResult, this).let {
                commands.add(it)
                return it
            }
        }
    }

    fun handler(type: HandlerTypes, exception: Sql, action: Sql): Handler {
        Handler(type, exception, action).let {
            commands.add(it)
            return it
        }
    }

    fun addFunction(script: Sql) {
        commands.add(ReturnTypes.Function(script))
    }
}