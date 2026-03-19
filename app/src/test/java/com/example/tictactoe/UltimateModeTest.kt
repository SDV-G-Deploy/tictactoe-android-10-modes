package com.example.tictactoe

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UltimateModeTest {

    @Test
    fun firstMove_allowsOnlyDirectedLocalBoard() {
        val mode = UltimateMode()
        mode.newGame()

        mode.tap(index = 0)
        val state = mode.render()

        assertEquals(setOf(0), state.ultimate!!.allowedLocals)
        assertEquals(0, state.ultimate.forcedLocal)
    }

    @Test
    fun disallowTapOutsideForcedBoard() {
        val mode = UltimateMode()
        mode.newGame()

        mode.tap(0) // force local 0
        mode.tap(40) // illegal now (local 4)

        val state = mode.render()
        assertTrue("Cell must stay empty for illegal move", state.cells[40].text.isEmpty())
        assertEquals(Mark.O, state.currentPlayer)
        assertEquals(0, state.ultimate!!.forcedLocal)
    }

    @Test
    fun enabledCellsAlwaysBelongToAllowedLocals() {
        val mode = UltimateMode()
        mode.newGame()

        // play several legal moves, picking first enabled each step
        repeat(12) {
            val state = mode.render()
            val next = state.cells.indexOfFirst { it.enabled }
            if (next >= 0) mode.tap(next)
        }

        val final = mode.render()
        final.cells.forEachIndexed { idx, cell ->
            if (!cell.enabled) return@forEachIndexed
            val row = idx / 9
            val col = idx % 9
            val local = (row / 3) * 3 + (col / 3)
            assertTrue(local in final.ultimate!!.allowedLocals)
            assertTrue(final.ultimate.localStates[local] == CellKind.EMPTY)
        }
    }
}
