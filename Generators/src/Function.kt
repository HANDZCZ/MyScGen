class Function(
    name: String,
    private val returnType: FunctionReturnTypes,
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

    fun addReturn(script: Sql) {
        commands.add(ReturnTypes.Function(Sql("return ($script)")))
    }
}

enum class FunctionReturnTypes {
    Bool {
        override fun toString(): String = name.toLowerCase()
    }
}