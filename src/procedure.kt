fun main() {
    println("Nothing:\n" + Procedure("test1").generateScript())

    println("\nWith params and variables:\n" + Procedure("test2").apply {
        handler(HandlerTypes.Continue, "exception", "action")
        list("query", dummy::class)
        variable("variableType")
        parameter("parameterType")
    }.generateScript())

    println("\nWith some functions:\n" + Procedure("test3").apply {
        addFunction("select 'myA';")
        addFunction(while_("condition") {
            "action"
        })
    }.generateScript())
}

class dummy(val id: Any = "dummy")