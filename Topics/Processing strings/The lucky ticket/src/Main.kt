fun main() {
    val string = readln()

    var sumFirst = 0
    var sumLast = 0

    for(index in 0 until 3 ) {
        sumFirst += string[index].toChar().digitToInt()
    }

    for(index in 3..string.lastIndex ) {
        sumLast += string[index].toChar().digitToInt()
    }

    print(if (sumFirst == sumLast) "Lucky" else "Regular")
}