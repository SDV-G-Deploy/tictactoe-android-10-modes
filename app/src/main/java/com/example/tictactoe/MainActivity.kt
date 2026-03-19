package com.example.tictactoe

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridLayout
import android.widget.ListView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    private lateinit var menuScreen: View
    private lateinit var gameScreen: View

    private lateinit var selectedModeTitle: TextView
    private lateinit var selectedModeDescription: TextView
    private lateinit var modeList: ListView
    private lateinit var startGameButton: Button

    private lateinit var gameModeTitle: TextView
    private lateinit var gameModeDescription: TextView
    private lateinit var statusText: TextView
    private lateinit var restartButton: Button
    private lateinit var switchModeButton: Button
    private lateinit var menuButton: Button
    private lateinit var boardGrid: GridLayout

    private val allModes = GameModesFactory.createAll()
    private var selectedModeIndex = 0
    private var currentMode: GameMode? = null
    private val cellButtons = mutableListOf<MaterialButton>()

    private val handler = Handler(Looper.getMainLooper())
    private val ticker = object : Runnable {
        override fun run() {
            currentMode?.tick()
            render()
            handler.postDelayed(this, 1_000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        setupModeList()
        setupButtons()
        updateSelectedModeCard()
        showMenuScreen()

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (gameScreen.visibility == View.VISIBLE) {
                        showMenuScreen()
                    } else {
                        finish()
                    }
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(ticker, 1_000)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(ticker)
    }

    private fun bindViews() {
        menuScreen = findViewById(R.id.menuScreen)
        gameScreen = findViewById(R.id.gameScreen)

        selectedModeTitle = findViewById(R.id.selectedModeTitle)
        selectedModeDescription = findViewById(R.id.selectedModeDescription)
        modeList = findViewById(R.id.modeList)
        startGameButton = findViewById(R.id.startGameButton)

        gameModeTitle = findViewById(R.id.gameModeTitle)
        gameModeDescription = findViewById(R.id.gameModeDescription)
        statusText = findViewById(R.id.statusText)
        restartButton = findViewById(R.id.restartButton)
        switchModeButton = findViewById(R.id.switchModeButton)
        menuButton = findViewById(R.id.menuButton)
        boardGrid = findViewById(R.id.boardGrid)
    }

    private fun setupModeList() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_single_choice,
            allModes.map { it.definition.title }
        )

        modeList.adapter = adapter
        modeList.choiceMode = ListView.CHOICE_MODE_SINGLE
        modeList.setItemChecked(selectedModeIndex, true)
        modeList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            selectedModeIndex = position
            updateSelectedModeCard()
        }
    }

    private fun setupButtons() {
        startGameButton.setOnClickListener {
            startSelectedMode()
            showGameScreen()
        }

        restartButton.setOnClickListener {
            currentMode?.newGame()
            render()
        }

        switchModeButton.setOnClickListener {
            showMenuScreen()
            modeList.smoothScrollToPosition(selectedModeIndex)
        }

        menuButton.setOnClickListener {
            showMenuScreen()
        }
    }

    private fun startSelectedMode() {
        currentMode = allModes[selectedModeIndex]
        currentMode?.newGame()
        render()
    }

    private fun showMenuScreen() {
        menuScreen.visibility = View.VISIBLE
        gameScreen.visibility = View.GONE
    }

    private fun showGameScreen() {
        menuScreen.visibility = View.GONE
        gameScreen.visibility = View.VISIBLE
    }

    private fun updateSelectedModeCard() {
        val mode = allModes[selectedModeIndex].definition
        selectedModeTitle.text = mode.title
        selectedModeDescription.text = mode.description
    }

    private fun render() {
        val state = currentMode?.render() ?: return

        gameModeTitle.text = state.modeName
        gameModeDescription.text = state.modeDescription
        statusText.text = state.status

        if (cellButtons.size != state.cells.size) {
            rebuildGrid(state.boardSize, state.cells.size)
        }

        state.cells.forEachIndexed { index, cell ->
            val button = cellButtons[index]
            button.text = cell.text
            button.isEnabled = cell.enabled
            button.alpha = if (cell.blocked) 0.5f else 1f
            button.setTextColor(colorForCell(cell.kind))
            button.strokeColor = getColorStateList(R.color.cell_border)
            button.backgroundTintList = getColorStateList(
                if (cell.blocked) R.color.cell_blocked else R.color.cell_bg
            )
        }
    }

    private fun colorForCell(kind: CellKind): Int {
        return when (kind) {
            CellKind.X -> getColor(R.color.mark_x)
            CellKind.O -> getColor(R.color.mark_o)
            CellKind.BLOCKED -> getColor(R.color.text_secondary)
            CellKind.EMPTY -> getColor(R.color.text_primary)
        }
    }

    private fun rebuildGrid(boardSize: Int, totalCells: Int) {
        boardGrid.removeAllViews()
        cellButtons.clear()

        boardGrid.columnCount = boardSize
        boardGrid.rowCount = boardSize

        val screenWidth = resources.displayMetrics.widthPixels
        val side = min(screenWidth - dp(32), dp(520))
        val cellSize = side / boardSize

        repeat(totalCells) { index ->
            val button = MaterialButton(
                this,
                null,
                com.google.android.material.R.attr.materialButtonOutlinedStyle
            ).apply {
                textSize = when {
                    boardSize <= 4 -> 24f
                    boardSize <= 6 -> 18f
                    else -> 13f
                }
                isAllCaps = false
                setPadding(0, 0, 0, 0)
                cornerRadius = dp(8)
                strokeWidth = dp(1)
                setOnClickListener {
                    currentMode?.tap(index)
                    render()
                }
            }

            val params = GridLayout.LayoutParams().apply {
                width = cellSize
                height = cellSize
                setMargins(dp(2), dp(2), dp(2), dp(2))
            }
            boardGrid.addView(button, params)
            cellButtons.add(button)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
