package com.professor314.blackjack.engine

data class StrategyRow(val hand: HandKey, val actions: List<Action>)

/**
 * Multi-deck (4-8 decks) basic strategy, adjusted for the rule variations in [Rules].
 * Every returned [Action] is final: fallbacks such as "double if allowed, otherwise
 * stand" are already resolved against the rules.
 */
object BasicStrategy {

    fun action(hand: HandKey, dealer: Int, rules: Rules): Action = when (hand.type) {
        HandType.HARD -> hard(hand.value, dealer, rules)
        HandType.SOFT -> soft(hand.value, dealer, rules)
        HandType.PAIR -> pair(hand.value, dealer, rules)
    }

    fun table(type: HandType, rules: Rules): List<StrategyRow> =
        Hands.of(type).map { hand ->
            StrategyRow(hand, DEALER_UPCARDS.map { action(hand, it, rules) })
        }

    /** [hardTotal] is null for soft hands. */
    private fun canDouble(hardTotal: Int?, rules: Rules): Boolean = when (rules.doubleRule) {
        DoubleRule.ANY_TWO -> true
        DoubleRule.NINE_TO_ELEVEN -> hardTotal != null && hardTotal in 9..11
        DoubleRule.TEN_ELEVEN -> hardTotal != null && hardTotal in 10..11
    }

    private fun hard(total: Int, d: Int, rules: Rules): Action {
        val h17 = rules.dealerHitsSoft17

        if (rules.surrenderAllowed) {
            val surrender = when (total) {
                15 -> d == 10 || (h17 && d == 11)
                16 -> d == 9 || d == 10 || d == 11
                17 -> h17 && d == 11
                else -> false
            }
            if (surrender) return Action.SURRENDER
        }

        fun doubleOrHit(wantDouble: Boolean) =
            if (wantDouble && canDouble(total, rules)) Action.DOUBLE else Action.HIT

        return when {
            total <= 8 -> Action.HIT
            total == 9 -> doubleOrHit(d in 3..6)
            total == 10 -> doubleOrHit(d in 2..9)
            total == 11 -> doubleOrHit(d in 2..10 || (h17 && d == 11))
            total == 12 -> if (d in 4..6) Action.STAND else Action.HIT
            total in 13..16 -> if (d in 2..6) Action.STAND else Action.HIT
            else -> Action.STAND
        }
    }

    /** [other] is the non-ace card (2..9). */
    private fun soft(other: Int, d: Int, rules: Rules): Action {
        val h17 = rules.dealerHitsSoft17

        // wantDouble: the chart says double. base: what to do when not doubling.
        val wantDouble: Boolean
        val base: Action
        when (other) {
            2, 3 -> {
                wantDouble = d in 5..6
                base = Action.HIT
            }
            4, 5 -> {
                wantDouble = d in 4..6
                base = Action.HIT
            }
            6 -> {
                wantDouble = d in 3..6
                base = Action.HIT
            }
            7 -> {
                wantDouble = d in 3..6 || (h17 && d == 2)
                base = if (d in 9..11) Action.HIT else Action.STAND
            }
            8 -> {
                wantDouble = h17 && d == 6
                base = Action.STAND
            }
            else -> {
                wantDouble = false
                base = Action.STAND
            }
        }
        return if (wantDouble && canDouble(null, rules)) Action.DOUBLE else base
    }

    /** [rank] is 2..10, or 11 for aces. */
    private fun pair(rank: Int, d: Int, rules: Rules): Action {
        val das = rules.doubleAfterSplit

        if (rank == 8 && d == 11 && rules.dealerHitsSoft17 && rules.surrenderAllowed) {
            return Action.SURRENDER
        }

        val split = when (rank) {
            2, 3 -> d in (if (das) 2..7 else 4..7)
            4 -> das && d in 5..6
            6 -> d in (if (das) 2..6 else 3..6)
            7 -> d in 2..7
            8, 11 -> true
            9 -> d in 2..6 || d == 8 || d == 9
            else -> false // 5s play as hard 10, 10s stand
        }
        if (split) return Action.SPLIT

        val hardTotal = if (rank == 11) 12 else rank * 2
        return hard(hardTotal, d, rules)
    }
}
