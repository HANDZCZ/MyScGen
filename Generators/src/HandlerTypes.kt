enum class HandlerTypes {
    Continue {
        override fun toString(): String = name.toLowerCase()
    }
}