abstract class BaseComponentWithParameter internal constructor(
    name: String,
    delimiter: String,
    nameGenerator: NameGenerator
) :
    BaseComponent(name, delimiter, nameGenerator) {
    fun parameter(type: DataTypes.generic, name: String = ""): ReturnTypes.Parameter = parameter(type.toString(), name)
    fun parameter(type: String, name: String = ""): ReturnTypes.Parameter {
        (if (name.isBlank()) nameGenerator.getNext() else name).let { name ->
            ReturnTypes.Parameter(name, type).let {
                commands.add(it)
                return it
            }
        }
    }
}