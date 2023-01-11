package connectfour

private const val SPACE = " "


class UserCommandLine : UserRepository {
    override fun askPlayerName(labelPlayer: String): String {
        println("$labelPlayer player's name:")
        return readln()
    }

    override fun askBoardSize(): String {
        println("Set the board dimensions (Rows x Columns)")
        println("Press Enter for default (6 x 7)")
        return readln()
    }

    override fun askNumberGame(): String {
        println("Do you want to play single or multiple games?")
        println("For a single game, input 1 or press Enter")
        println("Input a number of games:")
        return readln()
    }
}

interface UserRepository {
    fun askPlayerName(labelPlayer: String): String
    fun askBoardSize(): String
    fun askNumberGame(): String
}

class Game(private val userRepository: UserRepository) {

    private val player1: Player
    private val player2: Player
    private val board: Board
    private val numberGame: Int
    private var lastPlayer: Player? = null

    companion object {
        private const val LABEL_FIRST_PLAYER = "First"
        private const val LABEL_SECOND_PLAYER = "Second"
        private const val MIN_ROWS = 5
        private const val MAX_ROWS = 9

        private const val MIN_COLUMNS = 5
        private const val MAX_COLUMNS = 9

        private const val NUMBER_PAWNS_TO_WIN = 4
    }

    init {
        println("Connect Four")
        player1 = initPlayer(LABEL_FIRST_PLAYER, StateBoard.PLAYER_1)
        player2 = initPlayer(LABEL_SECOND_PLAYER, StateBoard.PLAYER_2)
        board = initBoard()
        numberGame = initNumberGame()
    }

    private fun initNumberGame(): Int {
        val numberGameUserInput: String = userRepository.askNumberGame()
        var numberGame = 1
        val regexNumberGame = Regex("[1-9]*")
        if (!regexNumberGame.matches(numberGameUserInput)) {
            println("Invalid input")
            numberGame = initNumberGame()
        }

        if (numberGameUserInput.isNotBlank()) {
            numberGame = numberGameUserInput.toInt()
        }
        return numberGame
    }

    private fun initPlayer(labelPlayer: String, stateBoard: StateBoard): Player {
        return Player(userRepository.askPlayerName(labelPlayer), stateBoard)
    }

    private fun initBoard(): Board {
        var board = Board()
        val inputUserBoard = userRepository.askBoardSize()
        if (inputUserBoard.isNotBlank()) {
            board = if (validateFormatInputBoard(inputUserBoard)) {
                val boardUserSize: List<Int> = inputUserBoard.uppercase().split("X").map { it.trim().toInt() }
                Board(boardUserSize[0], boardUserSize[1])
            } else {
                initBoard()
            }
        }
        return board
    }

    private fun validateFormatInputBoard(input: String): Boolean {
        val regexNumber = "\\d+".toRegex()
        val inputRowsColumns: List<Int> = input.uppercase().split("X")
            .map { it.trim() }
            .filter { it.isNotBlank() && regexNumber.matches(it) }
            .map { it.toInt() }

        if (inputRowsColumns.size != 2) {
            println("Invalid input")
            return false
        }

        if (inputRowsColumns[0] !in MIN_ROWS..MAX_ROWS) {
            println("Board rows should be from $MIN_ROWS to $MAX_ROWS")
            return false
        }
        if (inputRowsColumns[1] !in MIN_COLUMNS..MAX_COLUMNS) {
            println("Board columns should be from $MIN_COLUMNS to $MAX_COLUMNS")
            return false
        }

        return true
    }

    fun launch() {
        println("${player1.name} VS ${player2.name}")
        println("${board.rows} X ${board.columns} board")
        if (numberGame > 1) {
            multiple()
        } else {
            single()
        }
    }

    private fun single() {
        println("Single Game")
        this.start()
        printEndGame()
    }

    private fun multiple() {
        println("Total $numberGame games")
        for (game in 1..numberGame) {
            println("Game #${game}")
            this.start()
            println("Score")
            println("${player1.name}: ${player1.score} ${player2.name}: ${player2.score}")
        }
        printEndGame()
    }

    private fun start() {
        board.reset()
        board.print()
        var currentPlayer = changePlayerTurn(lastPlayer)
        while (true) {
            lastPlayer = currentPlayer
            val answerPlayer = currentPlayer.play()
            if (answerPlayer == "end") {
                break
            } else if (checkAnswerPlayer(answerPlayer)) {
                val validState = board.addState(answerPlayer, currentPlayer.stateBoard)
                if (validState) {
                    board.print()
                    if (checkIsDraw()) {
                        player1.incrementScore(1)
                        player2.incrementScore(1)
                        break
                    } else if (!checkPlayerWin(currentPlayer)) {
                        currentPlayer = changePlayerTurn(currentPlayer)
                    } else {
                        currentPlayer.incrementScore(2)
                        break
                    }
                }
            }
        }
    }

    private fun printEndGame() {
        println("Game over!")
    }

    private fun checkIsDraw(): Boolean {
        var end = false

        if (board.isFull()) {
            println("It is a draw")
            end = true
        }

        return end
    }

    private fun checkPlayerWin(player: Player): Boolean {
        var end = false

        if (board.calculateMaxPaws(player.stateBoard) == NUMBER_PAWNS_TO_WIN) {
            println("Player ${player.name} won")
            end = true
        }

        return end
    }

    private fun checkAnswerPlayer(answerPlayer: String): Boolean {
        val isValid = "\\d+".toRegex().matches(answerPlayer)
        if (!isValid) {
            println("Incorrect column number")
        }
        return isValid
    }

    private fun changePlayerTurn(currentPlayer: Player?): Player {
        return if (currentPlayer == player1) {
            player2
        } else {
            player1
        }
    }
}

enum class StateBoard(private val sign: String) {
    EMPTY(" "),
    PLAYER_1("o"),
    PLAYER_2("*");

    override fun toString(): String {
        return sign
    }
}

class Board(val rows: Int = 6, val columns: Int = 7) {

    private var lastRowPlayed: Int = -1
    private var lastColumnPlayed: Int = -1
    private lateinit var states: Array<Array<StateBoard>>

    companion object BoardConstants {
        private const val SIDE_BOARD = "║"
        private const val CORNER_LEFT_BOTTOM_BOARD = "╚"
        private const val CORNER_RIGHT_BOTTOM_BOARD = "╝"
        private const val BOTTOM_BOARD = "═"
        private const val JOINT_BOTTOM_BOARD = "╩"
        private const val ZERO_PAWS = 0
    }

    fun reset() {
        lastRowPlayed = -1
        lastColumnPlayed = -1
        states = Array(rows) {
            Array(columns) { StateBoard.EMPTY }
        }
    }

    fun print() {
        println("$SPACE${(1..columns).toList().joinToString(separator = SPACE)}$SPACE")

        (states.indices).forEach {
            println("$SIDE_BOARD${states[it].joinToString(separator = SIDE_BOARD)}$SIDE_BOARD")
        }

        println("$CORNER_LEFT_BOTTOM_BOARD${List(columns) { BOTTOM_BOARD }.joinToString(separator = JOINT_BOTTOM_BOARD)}$CORNER_RIGHT_BOTTOM_BOARD")
    }

    fun addState(columnNumber: String, statePlayer: StateBoard): Boolean {
        val isValid: Boolean = checkValidColumnNumber(columnNumber)
                && checkColumnIsFull(columnNumber.toInt())
        if (isValid) {
            val indexEmpty = states.withIndex()
                .reversed()
                .first { it.value[columnNumber.toInt() - 1] == StateBoard.EMPTY }
            states[indexEmpty.index][columnNumber.toInt() - 1] = statePlayer
            lastRowPlayed = indexEmpty.index
            lastColumnPlayed = columnNumber.toInt() - 1
        }
        return isValid
    }

    private fun checkValidColumnNumber(columnNumber: String): Boolean {
        val isValid = columnNumber.toInt() in 1..columns

        if (!isValid) {
            println("The column number is out of range (1 - $columns)")
        }

        return isValid
    }

    private fun checkColumnIsFull(columnNumber: Int): Boolean {
        val isNotFull = states.flatMap {
            listOf(it[columnNumber - 1])
        }
            .any {
                it == StateBoard.EMPTY
            }
        if (!isNotFull) {
            println("Column $columnNumber is full")
        }

        return isNotFull
    }

    fun isFull(): Boolean {
        var isFull = false
        try {
            states.flatMap {
                it.toList()
            }
                .first { it == StateBoard.EMPTY }
        } catch (e: NoSuchElementException) {
            isFull = true
        }

        return isFull
    }

    fun calculateMaxPaws(playerState: StateBoard): Int {
        return listOf(
            calculatePawnTo(lastRowPlayed, lastColumnPlayed, 0, 1, playerState)
                    + calculatePawnTo(lastRowPlayed, lastColumnPlayed, 0, -1, playerState) - 1,
            calculatePawnTo(lastRowPlayed, lastColumnPlayed, 1, 0, playerState)
                    + calculatePawnTo(lastRowPlayed, lastColumnPlayed, -1, 0, playerState) - 1,
            calculatePawnTo(lastRowPlayed, lastColumnPlayed, 1, 1, playerState)
                    + calculatePawnTo(lastRowPlayed, lastColumnPlayed, -1, -1, playerState) - 1,
            calculatePawnTo(lastRowPlayed, lastColumnPlayed, 1, -1, playerState)
                    + calculatePawnTo(lastRowPlayed, lastColumnPlayed, -1, 1, playerState) - 1,
        )
            .maxOrNull() ?: ZERO_PAWS
    }

    private fun calculatePawnTo(row: Int, column: Int, deltaRow: Int, deltaColumn: Int, stateBoard: StateBoard): Int {

        var numberPawns = 1

        var nextRow: Int = row + deltaRow
        var nextColumn: Int = column + deltaColumn

        while (nextRow in 0 until rows && nextColumn in 0 until columns) {
            if (states[nextRow][nextColumn] == stateBoard) {
                ++numberPawns
            } else {
                break
            }

            nextRow += deltaRow
            nextColumn += deltaColumn
        }

        return numberPawns
    }
}

data class Player(val name: String, val stateBoard: StateBoard, var score: Int = 0) {
    fun play(): String {
        println("${name}'s turn:")
        return readln()
    }

    fun incrementScore(points: Int) {
        score += points
    }
}

fun main() {
    val userRepository: UserRepository = UserCommandLine()
    val game = Game(userRepository)
    game.launch()
}
