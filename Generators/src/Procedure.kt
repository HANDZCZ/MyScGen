import ReturnTypes.Parameter

open class Procedure(
    name: String,
    delimiter: String = "!!",
    nameGenerator: NameGenerator = DefaultNameGenerator(),
    internal open val func: Procedure.() -> Unit
) : BaseComponent(name, delimiter, nameGenerator) {
    override fun callFunc() = func()

    override fun generateScript(): String = generateInnerScript().let {
        "delimiter $delimiter\n" +
                "drop procedure if exists $name !!\n" +
                "create procedure $name(" +
                it.params +
                ") begin\n" +
                it.body +
                "\nend $delimiter\n" +
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
}

//internal class InnerScripts(val params: String, val body: String)