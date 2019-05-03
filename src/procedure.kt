fun main() {
    println("Nothing:\n" + Procedure("test1", func = {}).generateScript())

    println("\nWith params and variables:\n" + Procedure("test2") {
        handler(HandlerTypes.Continue, "exception", "action")
        list("query", dummy::class)
        variable("variableType")
        parameter("parameterType")
    }.generateScript())

    println("\nWith some functions:\n" + Procedure("test3") {
        "select 'myA';".toCommand()
        while_("condition") {
            "action"
        }.toCommand()
    }.generateScript())
}

class dummy(val id: Any = "dummy") : ReturnTypes.Cursor.ExpectedResult()