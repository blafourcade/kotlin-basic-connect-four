fun main() {
    val n = readln().toInt()
    val numbers = MutableList(n) { readln().toInt() }
    val m = readln().toInt()

    print(if (m in numbers) "YES" else "NO")
}