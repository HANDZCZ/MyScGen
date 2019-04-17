interface NameGenerator {
    fun getNext(): String
}

internal class DefaultNameGenerator : NameGenerator {
    private var lastName = mutableListOf<Int>()
    override fun getNext(): String {
        if (lastName.isEmpty()) {
            lastName.add(97)
        } else {
            if (lastName.last().toByte() < 122) {
                lastName[lastName.lastIndex] += 1
            } else {
                lastName.add(97)
            }
        }
        return lastName.joinToString { it.toChar().toString() }
    }
}