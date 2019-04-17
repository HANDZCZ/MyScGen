import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberProperties

class ReturnTypes private constructor() {
    interface generic {
        val priority: Int
            get() = 0

        fun generateScript(): String
    }

    abstract class genericVar(val name: String, val typeAndParams: String) : generic {
        override fun toString(): String = name
    }

    class Parameter(name: String, typeAndParams: String) : genericVar(name, typeAndParams) {
        override fun generateScript(): String = "$name $typeAndParams"
    }

    class Variable(name: String, typeAndParams: String) : genericVar(name, typeAndParams) {
        override fun generateScript(): String = "declare $name $typeAndParams;"
    }

    class Cursor<T : Any>(name: String, val query: Sql, val item: KClass<T>, val procedure: Procedure) :
        genericVar(name, "") {
        override fun generateScript(): String = "declare $name cursor for ${query.script};"

        private var itemWithVariableNames: T? = null
        private fun getIWVN(): T {
            if (itemWithVariableNames != null) return itemWithVariableNames as T
            item.constructors.first().let { constructor ->
                val itemDataTypes = constructor.callBy(emptyMap())
                val parameters = mutableMapOf<KParameter, Any>()
                constructor.parameters.forEach { parameter ->
                    procedure.variable(
                        item.memberProperties.first { it.name == parameter.name }.getter.call(
                            itemDataTypes
                        ) as DataTypes.generic
                    ).let {
                        parameters[parameter] = it.name
                    }
                }
                itemWithVariableNames = constructor.callBy(parameters)
            }
            return itemWithVariableNames as T
        }

        private var stopVar: Variable? = null
        private var loopName: String? = null
        fun forEach(func: (T) -> Sql) {
            if (stopVar == null) {
                stopVar = procedure.variable(DataTypes.Bool_dt())
                procedure.handler(
                    HandlerTypes.Continue,
                    Sql("sqlstate '02000'"),
                    Sql("set $stopVar = true")
                )
                loopName = procedure.nameGenerator.getNext()
            }
            procedure.addFunction(
                Sql(
                    "set $stopVar = false;\n" +
                            "open $name;\n" +
                            "$loopName: loop " +
                            "fetch $name into ${item.memberProperties.joinToString(", ") {
                                it.getter.call(getIWVN()).toString()
                            }};\n" +
                            "if $stopVar then leave $loopName; end if;\n"
                ) +
                        func(getIWVN()) +
                        Sql(";\nend loop $loopName;\nclose $name")
            )
        }
    }

    class Handler(val type: HandlerTypes, val exception: Sql, val action: Sql) : generic {
        override fun generateScript(): String = "declare $type handler for $exception $action;"
    }

    class Function(val script: Sql) : generic {
        override fun generateScript(): String = "$script;"
    }
}