package com.professor314.blackjack.engine

/** The final action a player should take with their first two cards. */
enum class Action(val label: String, val short: String) {
    HIT("Hit", "H"),
    STAND("Stand", "S"),
    DOUBLE("Double", "D"),
    SPLIT("Split", "P"),
    SURRENDER("Surrender", "R"),
}

/** Which starting hands the casino lets you double on. */
enum class DoubleRule(val label: String, val shortLabel: String) {
    ANY_TWO("Any two cards", "Any"),
    NINE_TO_ELEVEN("Hard 9-11 only", "9-11"),
    TEN_ELEVEN("Hard 10-11 only", "10-11"),
}

data class Rules(
    val dealerHitsSoft17: Boolean = false,
    val doubleAfterSplit: Boolean = true,
    val surrenderAllowed: Boolean = false,
    val doubleRule: DoubleRule = DoubleRule.ANY_TWO,
)

enum class HandType { HARD, SOFT, PAIR }

/**
 * Identifies a row in the strategy table.
 *
 * - HARD: [value] is the hard total (5..17; 17 also stands for 17+).
 * - SOFT: [value] is the non-ace card (2..9), i.e. A,[value].
 * - PAIR: [value] is the pair rank (2..10, 11 = aces).
 */
data class HandKey(val type: HandType, val value: Int) {
    val label: String
        get() = when (type) {
            HandType.HARD -> if (value >= 17) "17+" else "$value"
            HandType.SOFT -> "A,$value"
            HandType.PAIR -> if (value == 11) "A,A" else "$value,$value"
        }
}

/** A practice question: a player hand against a dealer upcard (2..11, 11 = ace). */
data class Cell(val hand: HandKey, val dealer: Int)

/** Dealer upcards in table column order. 11 represents the ace. */
val DEALER_UPCARDS: List<Int> = (2..11).toList()

fun dealerLabel(upcard: Int): String = if (upcard == 11) "A" else upcard.toString()

object Hands {
    val hard: List<HandKey> = (5..17).map { HandKey(HandType.HARD, it) }
    val soft: List<HandKey> = (2..9).map { HandKey(HandType.SOFT, it) }
    val pairs: List<HandKey> = (2..11).map { HandKey(HandType.PAIR, it) }

    fun of(type: HandType): List<HandKey> = when (type) {
        HandType.HARD -> hard
        HandType.SOFT -> soft
        HandType.PAIR -> pairs
    }

    val all: List<HandKey> = hard + soft + pairs

    val allCells: List<Cell> = all.flatMap { hand -> DEALER_UPCARDS.map { Cell(hand, it) } }
}
