open class Procedure(
    name: String,
    delimiter: String = "!!",
    nameGenerator: NameGenerator = DefaultNameGenerator(),
    internal val func: Procedure.() -> Unit
) : BaseComponentWithParameter(name, delimiter, nameGenerator) {
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
}