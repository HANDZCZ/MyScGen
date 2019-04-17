import DataTypes.Int_dt
import java.util.*

fun main() {
    val vratNaSklad = Procedure("vratNaSklad", nameGenerator = zmrdogen()).apply {
        val idObjednavky = parameter(Int_dt(listOf("unsigned")), "idObjednavky")
        val polozky = list(
            Sql("select pocet, zbozi_id from polozkyobjednavky where objednavka_id = $idObjednavky"),
            objednavka::class
        )
        polozky.forEach {
            Sql("update sklad set pocet = pocet + ${it.pocet} where zbozi_id = ${it.zbozi_id}")
        }
    }
    println(vratNaSklad.generateScript())
}

val intType1 = Int_dt(listOf("unsigned"))

class objednavka(val pocet: Any = intType1, val zbozi_id: Any = intType1)

class zmrdogen : NameGenerator {
    override fun getNext(): String = "$${UUID.randomUUID().toString().replace("-", "$")}"
}