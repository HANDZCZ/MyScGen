class DataTypes private constructor() {
    abstract class generic(val params: List<String>) {
        override fun toString(): String =
            this::class.simpleName!!.split("_")[0].toLowerCase() +
                    (if (params.isNotEmpty()) " " else "") +
                    params.joinToString(" ")
    }

    class Int_dt(params: List<String> = emptyList()) : generic(params)
    class Bool_dt(params: List<String> = emptyList()) : generic(params)
    class Varchar_dt(val size: Int, params: List<String> = emptyList()) : generic(params) {
        override fun toString(): String = super.toString().let {
            "varchar($size)" + it.substring(7, it.length)
        }
    }
}