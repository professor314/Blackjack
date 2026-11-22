package com.professor314.blackjack.engine

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PracticeTest {
    private fun value(rank: String): Int = when (rank) {
        "A" -> 11
        "J", "Q", "K" -> 10
        else -> rank.toInt()
    }

    @Test
    fun dealsMatchTheirCell() {
        val random = Random(42)
        repeat(3) {
            for (cell in Hands.allCells) {
                val deal = HandDealer.deal(cell, random)
                assertEquals(2, deal.player.size)
                assertEquals(cell.dealer, value(deal.dealer.rank))
                val v = deal.player.map { value(it.rank) }
                when (cell.hand.type) {
                    HandType.PAIR -> assertEquals(listOf(cell.hand.value, cell.hand.value), v)
                    HandType.SOFT -> assertEquals(listOf(11, cell.hand.value), v.sortedDescending())
                    HandType.HARD -> {
                        assertEquals(cell.hand.value, v.sum(), "bad hard deal for $cell: $v")
                        assertTrue(v[0] != v[1] && 11 !in v, "hard hand must not be a pair or soft: $v")
                    }
                }
            }
        }
    }

    @Test
    fun weightsFavorMissedAndUnseenCells() {
        val stats = PracticeStats()
        val unseen = Cell(HandKey(HandType.HARD, 12), 10)
        val missed = Cell(HandKey(HandType.HARD, 13), 10)
        val known = Cell(HandKey(HandType.HARD, 14), 10)
        repeat(5) { stats.record(missed, false) }
        repeat(10) { stats.record(known, true) }

        assertTrue(stats.weight(missed) > stats.weight(known))
        assertTrue(stats.weight(unseen) > stats.weight(known))
        assertTrue(stats.weight(missed) > stats.weight(unseen))
    }

    @Test
    fun pickerAvoidsRepeatAndPrefersWeakCells() {
        val stats = PracticeStats()
        val weak = Cell(HandKey(HandType.PAIR, 8), 6)
        for (cell in Hands.allCells) {
            repeat(10) { stats.record(cell, cell != weak) }
        }
        val random = Random(7)
        var weakPicks = 0
        repeat(2000) {
            if (CellPicker.pick(stats, random) == weak) weakPicks++
        }
        // A uniform pick would give about 6 of 2000; the weights give about 40.
        assertTrue(weakPicks > 20, "weak cell picked only $weakPicks times")

        repeat(200) {
            assertTrue(CellPicker.pick(stats, random, exclude = weak) != weak)
        }
    }

    @Test
    fun masteryNeedsAttemptsAndAccuracy() {
        val stats = PracticeStats()
        val cell = Cell(HandKey(HandType.SOFT, 6), 5)
        stats.record(cell, true)
        assertEquals(0, stats.masteredCount())
        repeat(3) { stats.record(cell, true) }
        assertEquals(1, stats.masteredCount())
    }

    @Test
    fun statsRoundTrip() {
        val stats = PracticeStats()
        val a = Cell(HandKey(HandType.HARD, 16), 10)
        val b = Cell(HandKey(HandType.PAIR, 11), 11)
        stats.record(a, false)
        stats.record(a, true)
        stats.record(b, true)

        val decoded = StatsCodec.decode(StatsCodec.encode(stats.snapshot()))
        assertEquals(CellStats(2, 1), decoded[a])
        assertEquals(CellStats(1, 1), decoded[b])
        assertEquals(2, decoded.size)
    }

    @Test
    fun decodeIgnoresGarbage() {
        val decoded = StatsCodec.decode("nonsense\nHARD,12,3,1,1\nHARD,99,3,1,1\nPAIR,x,2,1,1")
        assertEquals(1, decoded.size)
    }
}
