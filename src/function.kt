fun main() {
    println("Nothing:\n" + Function("test1", "returnType", func = {}).generateScript())

    println("\nWith params and variables:\n" + Function("test2", "returnType") {
        handler(HandlerTypes.Continue, "exception", "action")
        list("query", dummy::class)
        variable("variableType")
        parameter("parameterType")
    }.generateScript())

    println("\nWith some functions:\n" + Function("test3", "returnType") {
        "select 'myA';".toCommand()
        while_("condition") {
            "action"
        }.toCommand()
    }.generateScript())
}