fun main() {
    val answer = readln()
    val regex = "I can('t)? do my homework on time!".toRegex()
    print(regex.matches(answer))
}