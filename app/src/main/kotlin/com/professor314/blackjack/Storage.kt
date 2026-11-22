package com.professor314.blackjack

import android.content.Context
import com.professor314.blackjack.engine.Cell
import com.professor314.blackjack.engine.CellStats
import com.professor314.blackjack.engine.DoubleRule
import com.professor314.blackjack.engine.Rules
import com.professor314.blackjack.engine.StatsCodec

/** Saves the selected rules and practice history in SharedPreferences. */
class Storage(context: Context) {
    private val prefs = context.getSharedPreferences("blackjack", Context.MODE_PRIVATE)

    fun loadRules(): Rules {
        val defaults = Rules()
        val doubleRule = DoubleRule.entries.firstOrNull { it.name == prefs.getString(KEY_DOUBLE_RULE, null) }
        return Rules(
            dealerHitsSoft17 = prefs.getBoolean(KEY_H17, defaults.dealerHitsSoft17),
            doubleAfterSplit = prefs.getBoolean(KEY_DAS, defaults.doubleAfterSplit),
            surrenderAllowed = prefs.getBoolean(KEY_SURRENDER, defaults.surrenderAllowed),
            doubleRule = doubleRule ?: defaults.doubleRule,
        )
    }

    fun saveRules(rules: Rules) {
        prefs.edit()
            .putBoolean(KEY_H17, rules.dealerHitsSoft17)
            .putBoolean(KEY_DAS, rules.doubleAfterSplit)
            .putBoolean(KEY_SURRENDER, rules.surrenderAllowed)
            .putString(KEY_DOUBLE_RULE, rules.doubleRule.name)
            .apply()
    }

    fun loadStats(): Map<Cell, CellStats> = StatsCodec.decode(prefs.getString(KEY_STATS, "") ?: "")

    fun saveStats(stats: Map<Cell, CellStats>) {
        prefs.edit().putString(KEY_STATS, StatsCodec.encode(stats)).apply()
    }

    private companion object {
        const val KEY_H17 = "h17"
        const val KEY_DAS = "das"
        const val KEY_SURRENDER = "surrender"
        const val KEY_DOUBLE_RULE = "double_rule"
        const val KEY_STATS = "stats"
    }
}
