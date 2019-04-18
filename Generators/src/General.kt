internal fun String.removeEndingSemicolons(): String {
    var res = this
    while (res.endsWith(';')) res = res.removeSuffix(';'.toString())
    return res
}