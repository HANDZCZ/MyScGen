fun main() {
    println("Nothing:\n" + Function("test1", "returnType").generateScript())

    println("\nWith params and variables:\n" + Function("test2", "returnType").apply {
        handler(HandlerTypes.Continue, "exception", "action")
        list("query", dummy::class)
        variable("variableType")
        parameter("parameterType")
    }.generateScript())

    println("\nWith some functions:\n" + Function("test3", "returnType").apply {
        addFunction("select 'myA';")
        addFunction(while_("condition") {
            "action"
        })
    }.generateScript())
}