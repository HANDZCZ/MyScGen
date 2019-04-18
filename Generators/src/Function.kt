class Function(
    name: String,
    private val returnType: String,
    delimiter: String = "!!",
    nameGenerator: NameGenerator = DefaultNameGenerator(),
    func: (procedure: Procedure) -> Unit = {}
) : Procedure(name, delimiter, nameGenerator, func) {
    override fun generateScript(): String {
        super.generateScript().let {
            val indexB = it.indexOf(") begin")
            return it.substring(0, indexB + 2).replaceFirst(
                "procedure",
                "function"
            ) + "returns $returnType" + it.substring(indexB + 1, it.length)
        }
    }

    fun return_(script: String): String = "return (${script.removeEndingSemicolons()});"
}