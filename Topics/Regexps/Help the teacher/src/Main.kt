fun main() {
    val report = readLine()!!
    val regex: Regex = Regex("\\d wrong answers?")
    //write your code here.
    println(regex.matches(report))
}