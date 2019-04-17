class Sql(script: String) {
    var script: String = script
        internal set(value) {
            field = value
        }

    override fun toString(): String = script
    internal operator fun plus(e: Sql): Sql = Sql(this.script + e.script)
    internal operator fun plusAssign(e: Sql) {
        this.script += e.script
    }
}