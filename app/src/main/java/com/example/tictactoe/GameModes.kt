package com.example.tictactoe

import kotlin.math.max
import kotlin.random.Random

enum class Mark(val symbol: String) {
    X("X"),
    O("O");

    fun other(): Mark = if (this == X) O else X
}

enum class CellKind {
    EMPTY,
    BLOCKED,
    X,
    O
}

data class CellViewState(
    val text: String,
    val enabled: Boolean,
    val blocked: Boolean = false
)

data class GameViewState(
    val boardSize: Int,
    val status: String,
    val cells: List<CellViewState>,
    val modeName: String,
    val modeDescription: String,
    val gameOver: Boolean
)

data class ModeDefinition(
    val id: String,
    val title: String,
    val description: String
)

interface GameMode {
    val definition: ModeDefinition
    fun newGame()
    fun tap(index: Int)
    fun tick() {}
    fun render(): GameViewState
}

private data class WinData(
    val hasWinner: Boolean,
    val winner: Mark? = null
)

private fun winnerForBoard(board: Array<CellKind>, size: Int, need: Int): Mark? {
    val directions = listOf(
        1 to 0,
        0 to 1,
        1 to 1,
        1 to -1
    )

    fun markAt(row: Int, col: Int): Mark? {
        if (row !in 0 until size || col !in 0 until size) return null
        return when (board[row * size + col]) {
            CellKind.X -> Mark.X
            CellKind.O -> Mark.O
            else -> null
        }
    }

    for (r in 0 until size) {
        for (c in 0 until size) {
            val start = markAt(r, c) ?: continue
            for ((dr, dc) in directions) {
                var ok = true
                for (step in 1 until need) {
                    if (markAt(r + dr * step, c + dc * step) != start) {
                        ok = false
                        break
                    }
                }
                if (ok) return start
            }
        }
    }
    return null
}

open class ConfigurableMode(
    override val definition: ModeDefinition,
    private val size: Int,
    private val winLength: Int,
    private val misere: Boolean = false,
    private val blockedCells: Int = 0,
    private val timedSeconds: Int? = null,
    private val bombEnabled: Boolean = false,
    private val gravityEnabled: Boolean = false
) : GameMode {

    private var board = Array(size * size) { CellKind.EMPTY }
    private var current = Mark.X
    private var gameOver = false
    private var status = ""
    private var secondsLeft = timedSeconds ?: 0
    private var bombIndex: Int? = null
    private var bombActive = false

    override fun newGame() {
        board = Array(size * size) { CellKind.EMPTY }
        current = Mark.X
        gameOver = false
        status = "Ход: ${current.symbol}"
        secondsLeft = timedSeconds ?: 0
        bombActive = bombEnabled

        if (blockedCells > 0) {
            val picks = board.indices.shuffled().take(blockedCells)
            picks.forEach { board[it] = CellKind.BLOCKED }
        }

        if (bombEnabled) {
            val candidates = board.indices.filter { board[it] == CellKind.EMPTY }
            bombIndex = candidates.randomOrNull()
        } else {
            bombIndex = null
        }

        if (timedSeconds != null) {
            status = "Ход: ${current.symbol} (осталось $secondsLeft c)"
        }
    }

    override fun tap(index: Int) {
        if (gameOver || index !in board.indices) return

        val targetIndex = if (gravityEnabled) lowestEmptyInColumn(index % size) else index
        if (targetIndex == -1) return
        if (board[targetIndex] != CellKind.EMPTY) return

        if (bombActive && bombIndex == targetIndex) {
            triggerBomb()
            return
        }

        board[targetIndex] = if (current == Mark.X) CellKind.X else CellKind.O

        val winner = winnerForBoard(board, size, winLength)
        if (winner != null) {
            gameOver = true
            if (misere) {
                val actualWinner = winner.other()
                status = "Линия у ${winner.symbol}: по misère победил ${actualWinner.symbol}"
            } else {
                status = "Победил: ${winner.symbol}"
            }
            return
        }

        if (board.none { it == CellKind.EMPTY }) {
            gameOver = true
            status = "Ничья"
            return
        }

        current = current.other()
        secondsLeft = timedSeconds ?: secondsLeft
        status = if (timedSeconds != null) {
            "Ход: ${current.symbol} (осталось $secondsLeft c)"
        } else {
            "Ход: ${current.symbol}"
        }
    }

    private fun triggerBomb() {
        status = "Бомба! ${current.symbol} теряет ход"
        val ownMarks = board.indices.filter { idx ->
            (current == Mark.X && board[idx] == CellKind.X) ||
                (current == Mark.O && board[idx] == CellKind.O)
        }
        if (ownMarks.isNotEmpty()) {
            val toClear = ownMarks.random()
            board[toClear] = CellKind.EMPTY
            status += ", одна фишка удалена"
        }
        bombActive = false
        bombIndex = null
        current = current.other()
        secondsLeft = timedSeconds ?: secondsLeft
        if (timedSeconds != null) {
            status += ". Ход: ${current.symbol} (осталось $secondsLeft c)"
        }
    }

    private fun lowestEmptyInColumn(column: Int): Int {
        for (row in size - 1 downTo 0) {
            val idx = row * size + column
            if (board[idx] == CellKind.EMPTY) return idx
        }
        return -1
    }

    override fun tick() {
        if (gameOver || timedSeconds == null) return
        secondsLeft -= 1
        if (secondsLeft <= 0) {
            current = current.other()
            secondsLeft = timedSeconds
            status = "Время вышло. Ход передан: ${current.symbol} (осталось $secondsLeft c)"
        } else {
            status = "Ход: ${current.symbol} (осталось $secondsLeft c)"
        }
    }

    override fun render(): GameViewState {
        val cellViews = board.mapIndexed { idx, cell ->
            val symbol = when (cell) {
                CellKind.X -> "X"
                CellKind.O -> "O"
                CellKind.BLOCKED -> "■"
                CellKind.EMPTY -> ""
            }
            CellViewState(
                text = symbol,
                enabled = !gameOver && cell == CellKind.EMPTY && (!gravityEnabled || hasEmptyInColumn(idx % size)),
                blocked = cell == CellKind.BLOCKED
            )
        }

        return GameViewState(
            boardSize = size,
            status = status,
            cells = cellViews,
            modeName = definition.title,
            modeDescription = definition.description,
            gameOver = gameOver
        )
    }

    private fun hasEmptyInColumn(column: Int): Boolean {
        for (row in 0 until size) {
            if (board[row * size + column] == CellKind.EMPTY) return true
        }
        return false
    }
}

class UltimateMode : GameMode {
    override val definition = ModeDefinition(
        id = "ultimate",
        title = "Ultimate (упрощённый)",
        description = "Поле 9x9 как 9 мини-полей 3x3. Клетка отправляет соперника в соответствующее мини-поле."
    )

    private val size = 9
    private var board = Array(size * size) { CellKind.EMPTY }
    private var localStatus = Array(9) { CellKind.EMPTY } // X/O/BLOCKED(draw)/EMPTY(active)
    private var current = Mark.X
    private var gameOver = false
    private var forcedLocal: Int? = null
    private var status = ""

    override fun newGame() {
        board = Array(size * size) { CellKind.EMPTY }
        localStatus = Array(9) { CellKind.EMPTY }
        current = Mark.X
        gameOver = false
        forcedLocal = null
        status = "Ход: ${current.symbol}. Можно в любое мини-поле"
    }

    override fun tap(index: Int) {
        if (gameOver || index !in board.indices || board[index] != CellKind.EMPTY) return
        val local = localIndexByCell(index)
        if (!isLocalAllowed(local)) return

        board[index] = if (current == Mark.X) CellKind.X else CellKind.O
        updateLocalResult(local)

        val globalWinner = globalWinner()
        if (globalWinner != null) {
            gameOver = true
            status = "Победил: ${globalWinner.symbol} (по мини-полям)"
            return
        }

        if (board.none { it == CellKind.EMPTY } || localStatus.all { it != CellKind.EMPTY }) {
            gameOver = true
            status = "Ничья"
            return
        }

        val nextLocal = nextForcedLocalFromCell(index)
        forcedLocal = if (localStatus[nextLocal] == CellKind.EMPTY) nextLocal else null

        current = current.other()
        status = if (forcedLocal == null) {
            "Ход: ${current.symbol}. Любое доступное мини-поле"
        } else {
            "Ход: ${current.symbol}. Обязательное мини-поле: ${forcedLocal!! + 1}"
        }
    }

    override fun render(): GameViewState {
        val cells = board.mapIndexed { idx, cell ->
            val local = localIndexByCell(idx)
            val isAllowed = isLocalAllowed(local)
            val text = when (cell) {
                CellKind.X -> "X"
                CellKind.O -> "O"
                else -> ""
            }
            val locallyDone = localStatus[local] != CellKind.EMPTY
            CellViewState(
                text = text,
                enabled = !gameOver && cell == CellKind.EMPTY && isAllowed && !locallyDone,
                blocked = locallyDone
            )
        }

        return GameViewState(
            boardSize = size,
            status = status,
            cells = cells,
            modeName = definition.title,
            modeDescription = definition.description,
            gameOver = gameOver
        )
    }

    private fun updateLocalResult(local: Int) {
        if (localStatus[local] != CellKind.EMPTY) return
        val mini = extractLocalBoard(local)
        val winner = winnerForBoard(mini, 3, 3)
        if (winner != null) {
            localStatus[local] = if (winner == Mark.X) CellKind.X else CellKind.O
            return
        }
        if (mini.none { it == CellKind.EMPTY }) {
            localStatus[local] = CellKind.BLOCKED // draw marker
        }
    }

    private fun globalWinner(): Mark? {
        val miniBoard = Array(9) { CellKind.EMPTY }
        for (i in 0 until 9) {
            miniBoard[i] = when (localStatus[i]) {
                CellKind.X -> CellKind.X
                CellKind.O -> CellKind.O
                else -> CellKind.EMPTY
            }
        }
        return winnerForBoard(miniBoard, 3, 3)
    }

    private fun isLocalAllowed(local: Int): Boolean {
        if (localStatus[local] != CellKind.EMPTY) return false
        return forcedLocal == null || forcedLocal == local
    }

    private fun localIndexByCell(index: Int): Int {
        val row = index / size
        val col = index % size
        return (row / 3) * 3 + (col / 3)
    }

    private fun nextForcedLocalFromCell(index: Int): Int {
        val row = index / size
        val col = index % size
        return (row % 3) * 3 + (col % 3)
    }

    private fun extractLocalBoard(local: Int): Array<CellKind> {
        val startRow = (local / 3) * 3
        val startCol = (local % 3) * 3
        val result = Array(9) { CellKind.EMPTY }
        var t = 0
        for (r in startRow until startRow + 3) {
            for (c in startCol until startCol + 3) {
                result[t++] = board[r * size + c]
            }
        }
        return result
    }
}

object GameModesFactory {
    fun createAll(): List<GameMode> = listOf(
        ConfigurableMode(
            definition = ModeDefinition("classic3", "Классика 3x3", "Обычные правила: 3 в ряд."),
            size = 3,
            winLength = 3
        ),
        ConfigurableMode(
            definition = ModeDefinition("grid4", "4x4 (4 в ряд)", "Большое поле 4x4 и победа по 4 в ряд."),
            size = 4,
            winLength = 4
        ),
        ConfigurableMode(
            definition = ModeDefinition("grid5_4", "5x5 (4 в ряд)", "Поле 5x5, побеждает линия из 4."),
            size = 5,
            winLength = 4
        ),
        ConfigurableMode(
            definition = ModeDefinition("grid5_5", "5x5 (5 в ряд)", "Поле 5x5, классический gomoku-lite: 5 в ряд."),
            size = 5,
            winLength = 5
        ),
        ConfigurableMode(
            definition = ModeDefinition("misere", "Misère 3x3", "Собрал 3 в ряд — проиграл."),
            size = 3,
            winLength = 3,
            misere = true
        ),
        UltimateMode(),
        ConfigurableMode(
            definition = ModeDefinition("blocked", "Random Blocked Cells", "На поле 5x5 случайно блокируются 5 клеток."),
            size = 5,
            winLength = 4,
            blockedCells = 5
        ),
        ConfigurableMode(
            definition = ModeDefinition("timed", "Timed Turn", "На каждый ход 10 секунд, иначе ход переходит сопернику."),
            size = 3,
            winLength = 3,
            timedSeconds = 10
        ),
        ConfigurableMode(
            definition = ModeDefinition("bomb", "Bomb Cell", "Одна скрытая бомба: при выборе ход теряется и удаляется одна фишка игрока."),
            size = 4,
            winLength = 4,
            bombEnabled = true
        ),
        ConfigurableMode(
            definition = ModeDefinition("gravity", "Gravity Mode", "Фишки падают вниз по столбцу. Победа: 4 в ряд."),
            size = 6,
            winLength = 4,
            gravityEnabled = true
        )
    )
}
