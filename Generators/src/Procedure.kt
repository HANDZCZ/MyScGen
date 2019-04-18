import ReturnTypes.*
import kotlin.reflect.KClass

open class Procedure(
    internal val name: String,
    internal val delimiter: String = "!!",
    internal val nameGenerator: NameGenerator = DefaultNameGenerator(),
    internal val func: (procedure: Procedure) -> Unit = {}
) {
    internal val commands = mutableListOf<ReturnTypes.generic>()

    internal fun generateInnerScript(): InnerScripts {
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

        return InnerScripts(
            commands.filter { it is Parameter }.let {
                (if (it.isNotEmpty()) "\n" else "") +
                        it.joinToString(
                            ",\n"
                        ) { command -> command.generateScript() } +
                        if (it.isNotEmpty()) "\n" else ""
            },
            commands.filter { it !is Parameter }.joinToString("\n") { it.generateScript() }
        )
    }

    open fun generateScript(): String = generateInnerScript().let {
        "delimiter $delimiter\n" +
                "drop procedure if exists $name !!\n" +
                "create procedure $name(" +
                it.params +
                ") begin\n" +
                it.body +
                "\nend procedure $delimiter\n" +
                "delimiter ;"
    }

    fun parameter(type: DataTypes.generic, name: String = ""): Parameter = parameter(type.toString(), name)
    fun parameter(type: String, name: String = ""): Parameter {
        (if (name.isBlank()) nameGenerator.getNext() else name).let { name ->
            Parameter(name, type).let {
                commands.add(it)
                return it
            }
        }
    }

    fun variable(type: DataTypes.generic, name: String = ""): Variable = variable(type.toString(), name)
    fun variable(type: String, name: String = ""): Variable {
        (if (name.isBlank()) nameGenerator.getNext() else name).let { name ->
            Variable(name, type).let {
                commands.add(it)
                return it
            }
        }
    }

    fun <T : Any> list(query: String, expectedResult: KClass<T>, name: String = ""): Cursor<T> {
        (if (name.isBlank()) nameGenerator.getNext() else name).let { name ->
            Cursor(name, query.removeEndingSemicolons(), expectedResult, this).let {
                commands.add(it)
                return it
            }
        }
    }

    fun handler(type: HandlerTypes, exception: String, action: String): Handler {
        Handler(type, exception, action.removeEndingSemicolons()).let {
            commands.add(it)
            return it
        }
    }

    fun addFunction(scriptAndVariable: Pair<String, Variable>) = addFunction(scriptAndVariable.first)
    fun addFunction(script: String) {
        commands.add(ReturnTypes.Function(script.removeEndingSemicolons()))
    }

    fun if_(
        condition: String,
        action: String,
        else_action: String = "",
        elseif: Map<String, String> = emptyMap()
    ): String =
        "if $condition then\n${action.removeEndingSemicolons()};" +
                elseif.map { it }.joinToString("\n") { "elseif ${it.key} then\n${it.value.removeEndingSemicolons()};" }.run run1@{ if (isBlank()) "" else "\n" + this@run1 } +
                "${if (else_action.isNotBlank()) "\nelse\n${else_action.removeEndingSemicolons()};" else ""}\nend if"

    fun for_(
        start: Int,
        end: Int,
        step: Int = 1,
        loopVariable: Variable = variable(DataTypes.Int_dt()),
        action: (String, String) -> String
    ): Pair<String, Variable> {
        val forLabel = nameGenerator.getNext()
        return Pair(
            "set $loopVariable = $start;\n" +
                    "$forLabel: while $loopVariable < $end do\n" +
                    action(forLabel, loopVariable.name).removeEndingSemicolons() + ";" +
                    "\nset $loopVariable = $loopVariable + $step;\n" +
                    "end while $forLabel;",
            loopVariable
        )
    }

    fun while_(condition: String, action: (String) -> String): String {
        val whileLabel = nameGenerator.getNext()
        return "$whileLabel: while ${condition.removeEndingSemicolons()} do\n" +
                action(whileLabel).removeEndingSemicolons() + ";" +
                "\nend while $whileLabel;"
    }
}

internal class InnerScripts(val params: String, val body: String)