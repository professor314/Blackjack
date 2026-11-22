package com.professor314.blackjack.engine

import kotlin.random.Random

data class CellStats(val attempts: Int = 0, val correct: Int = 0) {
    val wrong: Int get() = attempts - correct
}

/** Per-cell answer history, used to decide which hands to show next. */
class PracticeStats(initial: Map<Cell, CellStats> = emptyMap()) {
    private val map = initial.toMutableMap()

    operator fun get(cell: Cell): CellStats = map[cell] ?: CellStats()

    fun record(cell: Cell, correct: Boolean) {
        val old = get(cell)
        map[cell] = CellStats(old.attempts + 1, old.correct + if (correct) 1 else 0)
    }

    fun snapshot(): Map<Cell, CellStats> = map.toMap()

    fun reset() = map.clear()

    /** A cell counts as learned after a few attempts with a high hit rate. */
    fun isMastered(cell: Cell): Boolean {
        val s = get(cell)
        return s.attempts >= MASTERY_MIN_ATTEMPTS && s.correct.toDouble() / s.attempts >= MASTERY_ACCURACY
    }

    fun masteredCount(): Int = Hands.allCells.count { isMastered(it) }

    /**
     * Relative likelihood of showing [cell]. Unseen cells get a fixed high weight.
     * Seen cells use a smoothed miss rate, so a cell with many misses is drawn
     * often and a cell answered correctly many times in a row fades out.
     */
    fun weight(cell: Cell): Double {
        val s = get(cell)
        if (s.attempts == 0) return UNSEEN_WEIGHT
        val missRate = (s.wrong + 1.0) / (s.attempts + 2.0)
        return MIN_WEIGHT + MISS_SCALE * missRate
    }

    companion object {
        const val MASTERY_MIN_ATTEMPTS = 3
        const val MASTERY_ACCURACY = 0.8
        const val UNSEEN_WEIGHT = 3.0
        const val MIN_WEIGHT = 0.3
        const val MISS_SCALE = 4.0
    }
}

object CellPicker {
    /** Weighted random pick over all cells, never returning [exclude] (the previous cell). */
    fun pick(stats: PracticeStats, random: Random, exclude: Cell? = null): Cell {
        val candidates = Hands.allCells.filter { it != exclude }
        val weights = candidates.map { stats.weight(it) }
        var roll = random.nextDouble() * weights.sum()
        for (i in candidates.indices) {
            roll -= weights[i]
            if (roll < 0) return candidates[i]
        }
        return candidates.last()
    }
}

data class Card(val rank: String, val suit: Char) {
    val isRed: Boolean get() = suit == '\u2665' || suit == '\u2666'
    override fun toString() = "$rank$suit"
}

/** A dealt practice hand: two player cards and the dealer upcard for a [Cell]. */
data class Deal(val cell: Cell, val player: List<Card>, val dealer: Card)

object HandDealer {
    private val suits = charArrayOf('\u2660', '\u2665', '\u2666', '\u2663')
    private val tenRanks = listOf("10", "J", "Q", "K")

    private fun card(value: Int, random: Random): Card {
        val rank = when (value) {
            11, 1 -> "A"
            10 -> tenRanks.random(random)
            else -> value.toString()
        }
        return Card(rank, suits.random(random))
    }

    fun deal(cell: Cell, random: Random): Deal {
        val values: List<Int> = when (cell.hand.type) {
            HandType.PAIR -> listOf(cell.hand.value, cell.hand.value)
            HandType.SOFT -> listOf(11, cell.hand.value)
            HandType.HARD -> {
                val total = cell.hand.value
                val options = (2..10).filter { a -> val b = total - a; a < b && b in 3..10 }
                    .map { a -> listOf(a, total - a) }
                options.random(random)
            }
        }
        val player = values.shuffled(random).map { card(it, random) }
        return Deal(cell, player, card(cell.dealer, random))
    }
}

/** Text encoding for saving [PracticeStats], one cell per line. */
object StatsCodec {
    fun encode(stats: Map<Cell, CellStats>): String =
        stats.entries
            .filter { it.value.attempts > 0 }
            .joinToString("\n") { (cell, s) ->
                "${cell.hand.type.name},${cell.hand.value},${cell.dealer},${s.attempts},${s.correct}"
            }

    fun decode(text: String): Map<Cell, CellStats> {
        val result = mutableMapOf<Cell, CellStats>()
        for (line in text.lines()) {
            val parts = line.trim().split(",")
            if (parts.size != 5) continue
            val type = HandType.entries.firstOrNull { it.name == parts[0] } ?: continue
            val nums = parts.drop(1).mapNotNull { it.toIntOrNull() }
            if (nums.size != 4) continue
            val (value, dealer, attempts, correct) = nums
            val cell = Cell(HandKey(type, value), dealer)
            if (cell !in Hands.allCells) continue
            result[cell] = CellStats(attempts, correct)
        }
        return result
    }
}
