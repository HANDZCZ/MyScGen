fun main() {
    println(
        "Nothing:\n" + Event("test1", EventTimes.Every, "timeHERE", func = {}).generateScript()
    )

    println(
        "\nWith variables:\n" + Event("test2", EventTimes.Every, "timeHERE") {
            handler(HandlerTypes.Continue, "exception", "action")
            list("query", dummy::class)
            variable("variableType")
        }.generateScript()
    )

    println("\nWith some functions:\n" + Event("test3", EventTimes.Every, "timeHERE") {
        "select 'myA';".toCommand()
        while_("condition") {
            "insert into tableX (prop,pop,kop) value ('S','O','S')"
        }.toCommand()
    }.generateScript())
}