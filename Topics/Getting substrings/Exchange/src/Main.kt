fun main() {
    val string = readln()
    val newString = string.last() + string.substring(1, string.lastIndex) + string.first()
    print(newString)
}