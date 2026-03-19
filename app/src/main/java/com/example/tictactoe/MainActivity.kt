package com.example.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val allModes = GameModesFactory.createAll()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = gameColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    TicTacToeApp(allModes)
                }
            }
        }
    }
}

@Composable
private fun TicTacToeApp(modes: List<GameMode>) {
    var selectedModeIndex by remember { mutableIntStateOf(0) }
    var currentMode by remember { mutableStateOf<GameMode?>(null) }
    var viewState by remember { mutableStateOf<GameViewState?>(null) }
    var inGame by remember { mutableStateOf(false) }

    LaunchedEffect(currentMode, inGame) {
        while (inGame) {
            delay(1_000)
            currentMode?.tick()
            viewState = currentMode?.render()
        }
    }

    BackHandler(enabled = inGame) {
        inGame = false
    }

    if (!inGame) {
        MenuScreen(
            modes = modes,
            selectedModeIndex = selectedModeIndex,
            onSelect = { selectedModeIndex = it },
            onStart = {
                val mode = modes[selectedModeIndex]
                mode.newGame()
                currentMode = mode
                viewState = mode.render()
                inGame = true
            }
        )
    } else {
        val state = viewState ?: return
        GameScreen(
            state = state,
            onCellTap = { index ->
                currentMode?.tap(index)
                viewState = currentMode?.render()
            },
            onRestart = {
                currentMode?.newGame()
                viewState = currentMode?.render()
            },
            onSwitchMode = { inGame = false }
        )
    }
}

@Composable
private fun MenuScreen(
    modes: List<GameMode>,
    selectedModeIndex: Int,
    onSelect: (Int) -> Unit,
    onStart: () -> Unit
) {
    val selectedMode = modes[selectedModeIndex].definition

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Крестики-нолики", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("10 режимов в одном приложении", color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(14.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("Выбранный режим", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(selectedMode.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(selectedMode.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Все режимы", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                LazyColumn {
                    itemsIndexed(modes) { index, mode ->
                        val selected = index == selectedModeIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(index) }
                                .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selected) "●" else "○",
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.widthIn(min = 10.dp))
                            Text(mode.definition.title)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            ActionButton(text = "Начать игру", onClick = onStart)
        }
    }
}

@Composable
private fun GameScreen(
    state: GameViewState,
    onCellTap: (Int) -> Unit,
    onRestart: () -> Unit,
    onSwitchMode: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(state.modeName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(state.modeDescription, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(10.dp))
                    StatusChip(state)
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton("Рестарт", modifier = Modifier.weight(1f), onClick = onRestart)
                ActionButton("Сменить режим", modifier = Modifier.weight(1f), onClick = onSwitchMode)
            }
            Spacer(Modifier.height(10.dp))

            Board(
                state = state,
                onTap = onCellTap
            )
        }
    }
}

@Composable
private fun StatusChip(state: GameViewState) {
    val text = when {
        state.winner != null -> "Победа ${state.winner.symbol}"
        state.isDraw -> "Ничья"
        state.currentPlayer != null -> "Сейчас ход: ${state.currentPlayer.symbol}"
        else -> state.status
    }
    val color = when {
        state.winner == Mark.X -> Color(0xFF2167FF)
        state.winner == Mark.O -> Color(0xFFFF7A00)
        state.isDraw -> MaterialTheme.colorScheme.secondary
        state.currentPlayer == Mark.X -> Color(0xFF2167FF)
        state.currentPlayer == Mark.O -> Color(0xFFFF7A00)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.13f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Text(
            text = "$text · ${state.status}",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun Board(state: GameViewState, onTap: (Int) -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val side = maxWidth.coerceAtMost(560.dp)
        Card(
            modifier = Modifier
                .widthIn(max = side)
                .fillMaxWidth()
                .padding(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(Modifier.padding(8.dp)) {
                for (row in 0 until state.boardSize) {
                    Row {
                        for (col in 0 until state.boardSize) {
                            val index = row * state.boardSize + col
                            val cell = state.cells[index]
                            val localIndex = if (state.boardSize == 9) (row / 3) * 3 + (col / 3) else null
                            val blockBorder = state.ultimate?.let {
                                localIndex != null && it.forcedLocal == localIndex
                            } ?: false
                            val stroke = when {
                                state.boardSize == 9 && (row % 3 == 0 || col % 3 == 0) -> 2.dp
                                else -> 1.dp
                            }

                            val borderColor = when {
                                blockBorder -> MaterialTheme.colorScheme.primary
                                state.boardSize == 9 && (row % 3 == 0 || col % 3 == 0) -> MaterialTheme.colorScheme.outline
                                else -> MaterialTheme.colorScheme.outlineVariant
                            }

                            val bg = when {
                                cell.blocked -> MaterialTheme.colorScheme.surfaceVariant
                                cell.dimmed -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                blockBorder -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                else -> MaterialTheme.colorScheme.surface
                            }

                            val markColor = when (cell.kind) {
                                CellKind.X -> Color(0xFF2167FF)
                                CellKind.O -> Color(0xFFFF7A00)
                                CellKind.BLOCKED -> MaterialTheme.colorScheme.onSurfaceVariant
                                CellKind.EMPTY -> MaterialTheme.colorScheme.onSurface
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(1.5.dp)
                                    .background(bg, RoundedCornerShape(8.dp))
                                    .border(stroke, borderColor, RoundedCornerShape(8.dp))
                                    .sizeIn(minWidth = 36.dp, minHeight = 36.dp)
                                    .clickable(enabled = cell.enabled) { onTap(index) }
                                    .alpha(if (cell.dimmed) 0.7f else 1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cell.text,
                                    color = markColor,
                                    fontSize = when {
                                        state.boardSize <= 4 -> 34.sp
                                        state.boardSize <= 6 -> 24.sp
                                        else -> 17.sp
                                    },
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(vertical = 12.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun gameColorScheme() = androidx.compose.material3.lightColorScheme(
    primary = Color(0xFF335EFF),
    onPrimary = Color.White,
    background = Color(0xFFF4F6FB),
    surface = Color.White,
    onSurface = Color(0xFF1A2234),
    onSurfaceVariant = Color(0xFF5C6780),
    outline = Color(0xFF7A869C),
    outlineVariant = Color(0xFFC8D0E2),
    surfaceVariant = Color(0xFFE8EDF8),
    secondary = Color(0xFF56627B)
)
