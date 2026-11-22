package com.professor314.blackjack

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.professor314.blackjack.engine.Action
import com.professor314.blackjack.engine.Card

@Composable
fun PracticeScreen(vm: AppViewModel) {
    val deal = vm.deal
    val result = vm.result

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Session ${vm.sessionCorrect}/${vm.sessionAnswered}   |   Learned ${vm.masteredCount}/${vm.totalCells}",
            style = MaterialTheme.typography.titleSmall,
        )
        Text(rulesSummary(vm), style = MaterialTheme.typography.bodySmall)

        if (deal != null) {
            Spacer(Modifier.height(24.dp))
            Text("Dealer shows", style = MaterialTheme.typography.labelLarge)
            CardView(deal.dealer)
            Spacer(Modifier.height(24.dp))
            Text("Your hand", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                deal.player.forEach { CardView(it) }
            }
        }

        Spacer(Modifier.height(24.dp))
        val answering = result == null
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton(Action.HIT, answering, vm, Modifier.weight(1f))
            ActionButton(Action.STAND, answering, vm, Modifier.weight(1f))
            ActionButton(Action.DOUBLE, answering, vm, Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton(Action.SPLIT, answering, vm, Modifier.weight(1f))
            if (vm.rules.surrenderAllowed) {
                ActionButton(Action.SURRENDER, answering, vm, Modifier.weight(1f))
            }
        }

        if (result != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                if (result.isCorrect) {
                    "Correct: ${result.correctAction.label}"
                } else {
                    "Wrong. Correct play: ${result.correctAction.label}"
                },
                color = if (result.isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = vm::nextHand, modifier = Modifier.fillMaxWidth()) { Text("Next hand") }
        }

        Spacer(Modifier.height(32.dp))
        OutlinedButton(onClick = vm::resetProgress) { Text("Reset progress") }
    }
}

private fun rulesSummary(vm: AppViewModel): String {
    val r = vm.rules
    val parts = listOf(
        if (r.dealerHitsSoft17) "H17" else "S17",
        if (r.doubleAfterSplit) "DAS" else "no DAS",
        if (r.surrenderAllowed) "surrender" else "no surrender",
        "double ${r.doubleRule.shortLabel}",
    )
    return parts.joinToString(", ")
}

@Composable
private fun ActionButton(action: Action, enabled: Boolean, vm: AppViewModel, modifier: Modifier) {
    Button(onClick = { vm.answer(action) }, enabled = enabled, modifier = modifier) {
        Text(action.label)
    }
}

@Composable
private fun CardView(card: Card) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color.Gray),
        modifier = Modifier.size(width = 72.dp, height = 100.dp),
    ) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val color = if (card.isRed) Color(0xFFC62828) else Color.Black
            Text(card.rank, color = color, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(card.suit.toString(), color = color, fontSize = 28.sp)
        }
    }
}
