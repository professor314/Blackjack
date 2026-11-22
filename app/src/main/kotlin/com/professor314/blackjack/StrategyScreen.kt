package com.professor314.blackjack

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.professor314.blackjack.engine.Action
import com.professor314.blackjack.engine.BasicStrategy
import com.professor314.blackjack.engine.DEALER_UPCARDS
import com.professor314.blackjack.engine.DoubleRule
import com.professor314.blackjack.engine.HandType
import com.professor314.blackjack.engine.Rules
import com.professor314.blackjack.engine.dealerLabel

fun actionColor(action: Action): Color = when (action) {
    Action.HIT -> Color(0xFFFFD966)
    Action.STAND -> Color(0xFFF4A6A6)
    Action.DOUBLE -> Color(0xFF8FD18F)
    Action.SPLIT -> Color(0xFF8EB4E3)
    Action.SURRENDER -> Color(0xFFD0D0D0)
}

@Composable
fun StrategyScreen(rules: Rules, onRulesChange: (Rules) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
    ) {
        RulesPanel(rules, onRulesChange)
        Spacer(Modifier.height(12.dp))
        Legend()
        TableSection("Hard totals", HandType.HARD, rules)
        TableSection("Soft totals", HandType.SOFT, rules)
        TableSection("Pairs", HandType.PAIR, rules)
        Spacer(Modifier.height(8.dp))
        Text(
            "Multi-deck (4-8 decks) basic strategy. Columns are the dealer's upcard.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun RulesPanel(rules: Rules, onChange: (Rules) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("Rules", style = MaterialTheme.typography.titleMedium)
            SwitchRow("Dealer hits soft 17", rules.dealerHitsSoft17) {
                onChange(rules.copy(dealerHitsSoft17 = it))
            }
            SwitchRow("Double after split", rules.doubleAfterSplit) {
                onChange(rules.copy(doubleAfterSplit = it))
            }
            SwitchRow("Late surrender", rules.surrenderAllowed) {
                onChange(rules.copy(surrenderAllowed = it))
            }
            Text("Double on", Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DoubleRule.entries.forEach { option ->
                    FilterChip(
                        selected = rules.doubleRule == option,
                        onClick = { onChange(rules.copy(doubleRule = option)) },
                        label = { Text(option.shortLabel) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun Legend() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Action.entries.forEach { action ->
            GridCell("${action.short} ${action.label}", actionColor(action), fontSize = 10)
        }
    }
}

@Composable
private fun TableSection(title: String, type: HandType, rules: Rules) {
    val rows = remember(type, rules) { BasicStrategy.table(type, rules) }

    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
    )
    Row {
        GridCell("", Color.Transparent, weight = 1.5f)
        DEALER_UPCARDS.forEach { GridCell(dealerLabel(it), Color(0xFFE0E0E0), bold = true) }
    }
    rows.forEach { row ->
        Row {
            GridCell(row.hand.label, Color(0xFFE0E0E0), weight = 1.5f, bold = true)
            row.actions.forEach { GridCell(it.short, actionColor(it)) }
        }
    }
}

@Composable
private fun RowScope.GridCell(
    text: String,
    background: Color,
    weight: Float = 1f,
    bold: Boolean = false,
    fontSize: Int = 14,
) {
    Box(
        Modifier
            .weight(weight)
            .padding(1.dp)
            .height(32.dp)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = Color.Black,
            fontSize = fontSize.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
