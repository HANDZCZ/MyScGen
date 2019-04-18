fun main() {
    val func = Function("pocetOpravnenichLidi", "int").apply {
        val lidi = list("select id, jmeno, heslo, opravneni from users2", clovek::class)
        val pocet = variable("int unsigned default 0")

        lidi.forEach {
            if_(it.i4_opravneni as String, "set $pocet = $pocet + 1")
        }

        addFunction(return_("$pocet"))
    }
    println(func.generateScript())
}

class clovek(
    val i1_id: Any = "int(10) unsigned",
    val i2_jmeno: Any = DataTypes.Varchar_dt(20),
    val i3_heslo: Any = DataTypes.Varchar_dt(64),
    val i4_opravneni: Any = DataTypes.Bool_dt()
)