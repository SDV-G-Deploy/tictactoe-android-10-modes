package com.example.tictactoe

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    private lateinit var modeSpinner: Spinner
    private lateinit var modeDescription: TextView
    private lateinit var statusText: TextView
    private lateinit var newGameButton: Button
    private lateinit var boardGrid: GridLayout

    private val allModes = GameModesFactory.createAll()
    private var currentMode: GameMode? = null
    private val cellButtons = mutableListOf<Button>()

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

        modeSpinner = findViewById(R.id.modeSpinner)
        modeDescription = findViewById(R.id.modeDescription)
        statusText = findViewById(R.id.statusText)
        newGameButton = findViewById(R.id.newGameButton)
        boardGrid = findViewById(R.id.boardGrid)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            allModes.map { it.definition.title }
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        modeSpinner.adapter = adapter

        modeSpinner.setSelection(0)
        switchMode(0)

        modeSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                switchMode(position)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
        }

        newGameButton.setOnClickListener {
            currentMode?.newGame()
            render()
        }
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(ticker, 1_000)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(ticker)
    }

    private fun switchMode(position: Int) {
        currentMode = allModes[position]
        currentMode?.newGame()
        render()
    }

    private fun render() {
        val state = currentMode?.render() ?: return

        modeDescription.text = state.modeDescription
        statusText.text = state.status

        if (cellButtons.size != state.cells.size) {
            rebuildGrid(state.boardSize, state.cells.size)
        }

        state.cells.forEachIndexed { index, cell ->
            val button = cellButtons[index]
            button.text = cell.text
            button.isEnabled = cell.enabled
            button.alpha = if (cell.blocked) 0.45f else 1f
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
            val button = Button(this).apply {
                textSize = when {
                    boardSize <= 4 -> 24f
                    boardSize <= 6 -> 18f
                    else -> 13f
                }
                isAllCaps = false
                setPadding(0, 0, 0, 0)
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
