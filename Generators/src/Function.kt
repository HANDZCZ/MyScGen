class Function(
    name: String,
    private val returnType: String,
    delimiter: String = "!!",
    nameGenerator: NameGenerator = DefaultNameGenerator(),
    internal val func: Function.() -> Unit
) : BaseComponentWithParameter(name, delimiter, nameGenerator) {
    override fun callFunc() = func()

    override fun generateScript(): String = generateInnerScript().let {
        "delimiter $delimiter\n" +
                "drop function if exists $name !!\n" +
                "create function $name(" +
                it.params +
                ") returns $returnType begin\n" +
                it.body +
                "\nend $delimiter\n" +
                "delimiter ;"
    }

    fun return_(script: String): String = "return (${script.removeEndingSemicolons()});"
}