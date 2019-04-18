class Function(
    name: String,
    private val returnType: String,
    delimiter: String = "!!",
    nameGenerator: NameGenerator = DefaultNameGenerator(),
    func: (procedure: Procedure) -> Unit = {}
) : Procedure(name, delimiter, nameGenerator, func) {
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