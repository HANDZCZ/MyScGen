fun main() {
    Procedure("") {
        println(
            "For loop generation:\n" + for_(
                0,
                1,
                loopVariable = variable("loopVariable_type", "loopVariable_name")
            ) { label, variable ->
                "action"
            }.first
        )

        println(
            "\nIf generation:\n" + if_(
                "condition",
                "action",
                "else_action",
                mapOf(
                    "elseIfCondition1" to "elseIfAction1",
                    "elseIfCondition2" to "elseIfAction2",
                    "elseIfCondition3" to "elseIfAction3"
                )
            )
        )

        println("\nWhile loop generation:\n" + while_("condition") {
            "action"
        })

        println("\nException generation:\n" + throwException_(99999, "exception test"))
    }.generateScript()
}