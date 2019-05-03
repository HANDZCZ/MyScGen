import ReturnTypes.*
import kotlin.reflect.KClass

abstract class BaseComponent internal constructor(
    internal val name: String,
    internal val delimiter: String = "!!",
    internal val nameGenerator: NameGenerator = DefaultNameGenerator()
) {
    internal val commands = mutableListOf<ReturnTypes.Generic>()
    internal abstract fun callFunc()
    internal fun generateInnerScript(): InnerScripts {
        callFunc()
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

    abstract fun generateScript(): String

    fun variable(type: DataTypes.Generic, name: String = ""): Variable = variable(type.toString(), name)
    fun variable(type: String, name: String = ""): Variable {
        (if (name.isBlank()) nameGenerator.getNext() else name).let { alteredName ->
            Variable(alteredName, type).let {
                commands.add(it)
                return it
            }
        }
    }

    fun <T : Cursor.ExpectedResult> list(query: String, expectedResult: KClass<T>, name: String = ""): Cursor<T> {
        (if (name.isBlank()) nameGenerator.getNext() else name).let { alteredName ->
            Cursor(alteredName, query.removeEndingSemicolons(), expectedResult, this).let {
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

    fun Pair<String, Any>.toCommand() = this@BaseComponent.addCommand(this)
    fun String.toCommand() = this@BaseComponent.addCommand(this)
    fun addCommand(scriptAndVariable: Pair<String, Any>) = addCommand(scriptAndVariable.first)
    fun addCommand(script: String) {
        commands.add(ReturnTypes.Function(script.removeEndingSemicolons()))
    }

    fun if_(
        condition: String,
        action: String,
        else_action: String = "",
        elseif: Map<String, String> = emptyMap()
    ): String {
        return "if $condition then\n${action.removeEndingSemicolons()};" +
                elseif.map { it }.joinToString("\n") { "elseif ${it.key} then\n${it.value.removeEndingSemicolons()};" }.run run1@{ if (isBlank()) "" else "\n" + this@run1 } +
                "${if (else_action.isNotBlank()) "\nelse\n${else_action.removeEndingSemicolons()};" else ""}\nend if"
    }

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

    fun throwException_(sqlstate: Int, message: String): String {
        return "signal sqlstate '$sqlstate' set message_text = '$message';"
    }

    fun doWhile_(condition: String, action: (String) -> String): String {
        val whileLabel = nameGenerator.getNext()
        return "$whileLabel: repeat\n" +
                action(whileLabel).removeEndingSemicolons() + ";" +
                "\nuntil ${condition.removeEndingSemicolons()}\n" +
                "end repeat $whileLabel;"
    }
}

internal class InnerScripts(val params: String, val body: String)