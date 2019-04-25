class DataTypes private constructor() {
    abstract class Generic internal constructor(internal val params: List<String>) {
        override fun toString(): String =
            this::class.simpleName!!.split("_")[0].toLowerCase() +
                    (if (params.isNotEmpty()) " " else "") +
                    params.joinToString(" ")
    }

    class Int_dt(params: List<String> = emptyList()) : Generic(params)
    class Double_dt(params: List<String> = emptyList()) : Generic(params)
    class Bool_dt(params: List<String> = emptyList()) : Generic(params)
    class Varchar_dt(internal val size: Int, params: List<String> = emptyList()) : Generic(params) {
        override fun toString(): String = super.toString().let {
            "varchar($size)" + it.substring(7, it.length)
        }
    }
}