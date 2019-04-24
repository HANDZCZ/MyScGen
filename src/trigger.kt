fun main() {
    println(
        "Nothing:\n" + Trigger(
            TrigerTimes.Before,
            TrigerEvents.Insert,
            t1::class,
            "test1",
            func = {}).generateScript()
    )

    println(
        "\nWith variables:\n" + Trigger(
            TrigerTimes.Before,
            TrigerEvents.Insert,
            t1::class,
            "test2"
        ) { table ->
            handler(HandlerTypes.Continue, "exception", "action")
            list("query", dummy::class)
            variable("variableType")
        }.generateScript()
    )

    println("\nWith some functions:\n" + Trigger(TrigerTimes.Before, TrigerEvents.Insert, t1::class, "test3") { table ->
        "select 'myA';".toCommand()
        while_("condition") {
            "action new.${table.a},new.${table.c}"
        }.toCommand()
    }.generateScript())
}

class t1(val a: String, val b: String, val c: String) : Trigger.TriggerTable()