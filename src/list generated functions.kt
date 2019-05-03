fun main() {
    println("ForEach:\n" + Procedure("List") {
        val list = list("select 'query0'", user::class)

        list.forEach_ { s, user ->
            val s1 = list.forEach_ { label, item ->
                "select 'for body 1'"
            }
            val s2 = list.forEach_ { label, item ->
                "select 'for body 2'"
            }
            "$s1\n$s2"
        }.toCommand()

        list.forEach_ { s, dummy ->
            "select 'another for body'"
        }.toCommand()
    }.generateScript())

    println("\nJoinToString:\n" + Function("List", "blob") {
        val list = list("select id,jmeno,heslo from users2", user::class)
        val (script, joinedVar) = list.joinToString_("\\n") {
            mapOf("${it.i1_id}" to "-", "${it.i2_jmeno}" to ":", "${it.i3_heslo}" to "")
        }
        script.toCommand()
        return_("$joinedVar").toCommand()
    }.generateScript())
}

class user(val i1_id: Any = "int(10) unsigned", val i2_jmeno: Any = "varchar(20)", val i3_heslo: Any = "varchar(64)") :
    ReturnTypes.Cursor.ExpectedResult()