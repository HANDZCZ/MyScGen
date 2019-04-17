enum class HandlerTypes {
    Continue {
        override fun toString(): String = name.toLowerCase()
    },
    Exit {
        override fun toString(): String = name.toLowerCase()
    },
    Undo {
        override fun toString(): String = name.toLowerCase()
    }
}