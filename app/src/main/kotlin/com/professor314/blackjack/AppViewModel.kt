package com.professor314.blackjack

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.professor314.blackjack.engine.Action
import com.professor314.blackjack.engine.BasicStrategy
import com.professor314.blackjack.engine.Cell
import com.professor314.blackjack.engine.CellPicker
import com.professor314.blackjack.engine.Deal
import com.professor314.blackjack.engine.HandDealer
import com.professor314.blackjack.engine.Hands
import com.professor314.blackjack.engine.PracticeStats
import com.professor314.blackjack.engine.Rules
import kotlin.random.Random

data class AnswerResult(val chosen: Action, val correctAction: Action) {
    val isCorrect: Boolean get() = chosen == correctAction
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = Storage(application)
    private val stats = PracticeStats(storage.loadStats())
    private val random = Random.Default

    var rules by mutableStateOf(storage.loadRules())
        private set

    var deal by mutableStateOf<Deal?>(null)
        private set

    var result by mutableStateOf<AnswerResult?>(null)
        private set

    var sessionAnswered by mutableIntStateOf(0)
        private set

    var sessionCorrect by mutableIntStateOf(0)
        private set

    var masteredCount by mutableIntStateOf(stats.masteredCount())
        private set

    val totalCells: Int = Hands.allCells.size

    init {
        nextHand()
    }

    fun updateRules(newRules: Rules) {
        rules = newRules
        storage.saveRules(newRules)
    }

    fun answer(chosen: Action) {
        val current = deal ?: return
        if (result != null) return
        val correct = BasicStrategy.action(current.cell.hand, current.cell.dealer, rules)
        val answerResult = AnswerResult(chosen, correct)
        stats.record(current.cell, answerResult.isCorrect)
        storage.saveStats(stats.snapshot())
        sessionAnswered += 1
        if (answerResult.isCorrect) sessionCorrect += 1
        masteredCount = stats.masteredCount()
        result = answerResult
    }

    fun nextHand() {
        val previous: Cell? = deal?.cell
        val cell = CellPicker.pick(stats, random, exclude = previous)
        deal = HandDealer.deal(cell, random)
        result = null
    }

    fun resetProgress() {
        stats.reset()
        storage.saveStats(emptyMap())
        sessionAnswered = 0
        sessionCorrect = 0
        masteredCount = 0
        nextHand()
    }
}
