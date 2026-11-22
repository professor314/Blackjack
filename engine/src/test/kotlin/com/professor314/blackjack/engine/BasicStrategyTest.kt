package com.professor314.blackjack.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BasicStrategyTest {
    private val s17 = Rules(dealerHitsSoft17 = false, doubleAfterSplit = true, surrenderAllowed = false)
    private val h17 = s17.copy(dealerHitsSoft17 = true)

    private fun hard(total: Int) = HandKey(HandType.HARD, total)
    private fun soft(other: Int) = HandKey(HandType.SOFT, other)
    private fun pair(rank: Int) = HandKey(HandType.PAIR, rank)

    @Test
    fun hardTotals() {
        assertEquals(Action.HIT, BasicStrategy.action(hard(8), 6, s17))
        assertEquals(Action.HIT, BasicStrategy.action(hard(9), 2, s17))
        assertEquals(Action.DOUBLE, BasicStrategy.action(hard(9), 3, s17))
        assertEquals(Action.DOUBLE, BasicStrategy.action(hard(10), 9, s17))
        assertEquals(Action.HIT, BasicStrategy.action(hard(10), 10, s17))
        assertEquals(Action.DOUBLE, BasicStrategy.action(hard(11), 10, s17))
        assertEquals(Action.HIT, BasicStrategy.action(hard(12), 3, s17))
        assertEquals(Action.STAND, BasicStrategy.action(hard(12), 4, s17))
        assertEquals(Action.STAND, BasicStrategy.action(hard(16), 6, s17))
        assertEquals(Action.HIT, BasicStrategy.action(hard(16), 7, s17))
        assertEquals(Action.STAND, BasicStrategy.action(hard(17), 11, s17))
    }

    @Test
    fun softTotals() {
        assertEquals(Action.HIT, BasicStrategy.action(soft(2), 4, s17))
        assertEquals(Action.DOUBLE, BasicStrategy.action(soft(2), 5, s17))
        assertEquals(Action.DOUBLE, BasicStrategy.action(soft(6), 3, s17))
        assertEquals(Action.HIT, BasicStrategy.action(soft(6), 2, s17))
        assertEquals(Action.STAND, BasicStrategy.action(soft(7), 2, s17))
        assertEquals(Action.DOUBLE, BasicStrategy.action(soft(7), 6, s17))
        assertEquals(Action.STAND, BasicStrategy.action(soft(7), 8, s17))
        assertEquals(Action.HIT, BasicStrategy.action(soft(7), 9, s17))
        assertEquals(Action.STAND, BasicStrategy.action(soft(8), 6, s17))
        assertEquals(Action.STAND, BasicStrategy.action(soft(9), 6, s17))
    }

    @Test
    fun pairs() {
        assertEquals(Action.SPLIT, BasicStrategy.action(pair(11), 11, s17))
        assertEquals(Action.SPLIT, BasicStrategy.action(pair(8), 10, s17))
        assertEquals(Action.STAND, BasicStrategy.action(pair(10), 6, s17))
        assertEquals(Action.DOUBLE, BasicStrategy.action(pair(5), 9, s17))
        assertEquals(Action.HIT, BasicStrategy.action(pair(5), 10, s17))
        assertEquals(Action.SPLIT, BasicStrategy.action(pair(9), 9, s17))
        assertEquals(Action.STAND, BasicStrategy.action(pair(9), 7, s17))
        assertEquals(Action.SPLIT, BasicStrategy.action(pair(7), 7, s17))
        assertEquals(Action.HIT, BasicStrategy.action(pair(7), 8, s17))
        assertEquals(Action.SPLIT, BasicStrategy.action(pair(4), 5, s17))
        assertEquals(Action.HIT, BasicStrategy.action(pair(4), 7, s17))
    }

    @Test
    fun dealerHitsSoft17ChangesThreeDoubles() {
        assertEquals(Action.HIT, BasicStrategy.action(hard(11), 11, s17))
        assertEquals(Action.DOUBLE, BasicStrategy.action(hard(11), 11, h17))
        assertEquals(Action.STAND, BasicStrategy.action(soft(7), 2, s17))
        assertEquals(Action.DOUBLE, BasicStrategy.action(soft(7), 2, h17))
        assertEquals(Action.STAND, BasicStrategy.action(soft(8), 6, s17))
        assertEquals(Action.DOUBLE, BasicStrategy.action(soft(8), 6, h17))
    }

    @Test
    fun noDoubleAfterSplitChangesSmallPairs() {
        val noDas = s17.copy(doubleAfterSplit = false)
        assertEquals(Action.SPLIT, BasicStrategy.action(pair(2), 2, s17))
        assertEquals(Action.HIT, BasicStrategy.action(pair(2), 2, noDas))
        assertEquals(Action.SPLIT, BasicStrategy.action(pair(6), 2, s17))
        assertEquals(Action.HIT, BasicStrategy.action(pair(6), 2, noDas))
        assertEquals(Action.SPLIT, BasicStrategy.action(pair(4), 5, s17))
        assertEquals(Action.HIT, BasicStrategy.action(pair(4), 5, noDas))
        // Unaffected pairs
        assertEquals(Action.SPLIT, BasicStrategy.action(pair(8), 6, noDas))
    }

    @Test
    fun surrender() {
        val sur = s17.copy(surrenderAllowed = true)
        val surH17 = h17.copy(surrenderAllowed = true)
        assertEquals(Action.HIT, BasicStrategy.action(hard(16), 10, s17))
        assertEquals(Action.SURRENDER, BasicStrategy.action(hard(16), 10, sur))
        assertEquals(Action.SURRENDER, BasicStrategy.action(hard(16), 9, sur))
        assertEquals(Action.SURRENDER, BasicStrategy.action(hard(16), 11, sur))
        assertEquals(Action.SURRENDER, BasicStrategy.action(hard(15), 10, sur))
        assertEquals(Action.HIT, BasicStrategy.action(hard(15), 11, sur))
        assertEquals(Action.SURRENDER, BasicStrategy.action(hard(15), 11, surH17))
        assertEquals(Action.STAND, BasicStrategy.action(hard(17), 11, sur))
        assertEquals(Action.SURRENDER, BasicStrategy.action(hard(17), 11, surH17))
        assertEquals(Action.SPLIT, BasicStrategy.action(pair(8), 11, sur))
        assertEquals(Action.SURRENDER, BasicStrategy.action(pair(8), 11, surH17))
        assertEquals(Action.SPLIT, BasicStrategy.action(pair(8), 11, h17))
    }

    @Test
    fun surrenderNeverAppearsWhenDisallowed() {
        for (h in listOf(true, false)) {
            val rules = Rules(dealerHitsSoft17 = h, surrenderAllowed = false)
            for (cell in Hands.allCells) {
                assertTrue(BasicStrategy.action(cell.hand, cell.dealer, rules) != Action.SURRENDER)
            }
        }
    }

    @Test
    fun doubleRestrictions() {
        val nine = s17.copy(doubleRule = DoubleRule.NINE_TO_ELEVEN)
        val ten = s17.copy(doubleRule = DoubleRule.TEN_ELEVEN)
        assertEquals(Action.DOUBLE, BasicStrategy.action(hard(9), 5, nine))
        assertEquals(Action.HIT, BasicStrategy.action(hard(9), 5, ten))
        assertEquals(Action.DOUBLE, BasicStrategy.action(hard(10), 5, ten))
        // Soft hands cannot double under a restriction; A,7 falls back to stand.
        assertEquals(Action.HIT, BasicStrategy.action(soft(6), 5, nine))
        assertEquals(Action.STAND, BasicStrategy.action(soft(7), 5, nine))
        // 5,5 plays as hard 10, which both restrictions allow.
        assertEquals(Action.DOUBLE, BasicStrategy.action(pair(5), 5, ten))
    }

    @Test
    fun doubleOnlyWhereAllowed() {
        val nine = s17.copy(doubleRule = DoubleRule.NINE_TO_ELEVEN)
        for (cell in Hands.allCells) {
            if (BasicStrategy.action(cell.hand, cell.dealer, nine) != Action.DOUBLE) continue
            val total = when (cell.hand.type) {
                HandType.HARD -> cell.hand.value
                HandType.PAIR -> cell.hand.value * 2
                HandType.SOFT -> -1
            }
            assertTrue(total in 9..11, "unexpected double on $cell")
        }
    }

    @Test
    fun tableShape() {
        val t = BasicStrategy.table(HandType.HARD, s17)
        assertEquals(13, t.size)
        assertTrue(t.all { it.actions.size == 10 })
        assertEquals(8, BasicStrategy.table(HandType.SOFT, s17).size)
        assertEquals(10, BasicStrategy.table(HandType.PAIR, s17).size)
    }

    @Test
    fun cellCount() {
        assertEquals(310, Hands.allCells.size)
        assertFalse(Hands.allCells.isEmpty())
    }
}
