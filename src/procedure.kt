import DataTypes.Int_dt

fun main() {
    val proc = Procedure("vratNaSklad").apply {
        val idObjednavky = parameter(Int_dt(listOf("unsigned")), "idObjednavky")
        val polozky = list(
            "select pocet, zbozi_id from polozkyobjednavky where objednavka_id = $idObjednavky",
            objednavka::class
        )
        polozky.forEach {
            "update sklad set pocet = pocet + ${it.pocet} where zbozi_id = ${it.zbozi_id}"
        }
    }
    println(proc.generateScript())
}

val intType1 = Int_dt(listOf("unsigned"))

class objednavka(val pocet: Any = intType1, val zbozi_id: Any = intType1)