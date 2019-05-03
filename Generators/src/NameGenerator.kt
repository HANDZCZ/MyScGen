import java.util.*

interface NameGenerator {
    fun getNext(): String
}

internal class DefaultNameGenerator : NameGenerator {
    private val lastName = mutableListOf('`')

    override fun getNext(): String {
        for (x in 0 until lastName.size) {
            lastName[x] = lastName[x] + 1
            if (lastName[x] == '{') {
                lastName[x] = 'a'
            } else {
                return lastName.reversed().joinToString("")
            }
        }
        lastName.add('a')
        return lastName.reversed().joinToString("")
    }
}

class UUIDGen private constructor() {
    companion object : NameGenerator {
        override fun getNext(): String = "$${UUID.randomUUID().toString().replace("-", "$")}"
    }
}