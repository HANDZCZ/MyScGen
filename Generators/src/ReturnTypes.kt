import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberProperties

class ReturnTypes private constructor() {
    interface Generic {
        val priority: Int
            get() = 0

        fun generateScript(): String
    }

    abstract class GenericVar internal constructor(internal val name: String, internal val typeAndParams: String) :
        Generic {
        override fun toString(): String = name
    }

    class Parameter internal constructor(name: String, typeAndParams: String) : GenericVar(name, typeAndParams) {
        override fun generateScript(): String = "$name $typeAndParams"
    }

    class Variable internal constructor(name: String, typeAndParams: String) : GenericVar(name, typeAndParams) {
        override fun generateScript(): String = "declare $name $typeAndParams;"
    }

    class Cursor<T : Cursor.ExpectedItem> internal constructor(
        name: String,
        internal val query: String,
        internal val item: KClass<T>,
        internal val procedure: BaseComponent
    ) : GenericVar(name, "") {
        abstract class ExpectedItem

        override fun generateScript(): String = "declare $name cursor for $query;"

        private var noLoopsOpened = 0
        private val neededThings = mutableMapOf<Int, ItemAndStopVar<T>>()

        private fun generateIWVN(): T {
            item.constructors.first().let { constructor ->
                val itemDataTypes = constructor.callBy(emptyMap())
                val parameters = mutableMapOf<KParameter, Any>()
                constructor.parameters.forEach { parameter ->
                    val paramValue =
                        item.memberProperties.first { it.name == parameter.name }.getter.call(itemDataTypes)
                    procedure.variable(
                        when (paramValue) {
                            is DataTypes.Generic -> paramValue.toString()
                            is String -> paramValue
                            else -> throw Exception("Unsupported type")
                        }
                    ).let {
                        parameters[parameter] = it.name
                    }
                }
                constructor.callBy(parameters).let { generatedItem ->
                    return generatedItem
                }
            }
        }

        private fun generateSV(): Variable {
            val newVar = procedure.variable(DataTypes.Bool_dt())
            procedure.handler(
                HandlerTypes.Continue,
                "sqlstate '02000'",
                "set $newVar = true"
            )
            return newVar
        }

        private fun getNeededThings(): ItemAndStopVar<T> {
            return neededThings[noLoopsOpened] ?: ItemAndStopVar(generateIWVN(), generateSV()).also {
                neededThings[noLoopsOpened] = it
            }
        }

        fun forEach_(
            func: (label: String, item: T) -> String
        ): String {
            val generatedLoopLabel = procedure.nameGenerator.getNext()
            val (generatedItem, generatedStopVar) = getNeededThings()
            return "set $generatedStopVar = false;\n" +
                    kotlin.run {
                        noLoopsOpened++
                        if (noLoopsOpened == 1) "open $name;\n" else ""
                    } +
                    "$generatedLoopLabel: loop " +
                    "fetch $name into ${item.memberProperties.joinToString(", ") {
                        it.getter.call(generatedItem).toString()
                    }};\n" +
                    "if $generatedStopVar then leave $generatedLoopLabel; end if;\n" +
                    func(generatedLoopLabel, generatedItem).removeEndingSemicolons() +
                    ";\nend loop $generatedLoopLabel;" +
                    kotlin.run {
                        noLoopsOpened--
                        if (noLoopsOpened == 0) "\nclose $name;" else ""
                    }
        }


        fun joinToString_(
            separator: String,
            prefix: String = "",
            posfix: String = "",
            fields: (item: T) -> Map<String, String>
        ): Pair<String, Variable> {
            val str = procedure.variable("blob default '$prefix'")
            return Pair(
                this.forEach_ { s, item ->
                    "set ${str.name} = concat(${str.name}," +
                            fields(item).entries.joinToString(",") {
                                it.key + (if (it.value.isNotBlank()) ",'${it.value}'" else "")
                            } +
                            ",'$separator')"
                } + (if (posfix.isNotBlank()) "\nset ${str.name} = concat(${str.name},$posfix);" else ""),
                str
            )
        }
    }

    class Handler internal constructor(
        internal val type: HandlerTypes,
        internal val exception: String,
        internal val action: String
    ) :
        Generic {
        override fun generateScript(): String =
            "declare $type handler for $exception ${action.removeEndingSemicolons()};"
    }

    class Function internal constructor(internal val script: String) : Generic {
        override fun generateScript(): String = "${script.removeEndingSemicolons()};"
    }
}

internal class ItemAndStopVar<T : ReturnTypes.Cursor.ExpectedItem>(val item: T, var stopVar: ReturnTypes.Variable) {
    operator fun component1(): T = item
    operator fun component2(): ReturnTypes.Variable = stopVar
}