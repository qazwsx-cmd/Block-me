package com.blockme.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.blockme.core.domain.model.FocusGoal

/**
 * Focus goal text input with recent goals as quick-select chips.
 * SPDX-License-Identifier: MIT
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FocusGoalInput(
    value: String,
    onValueChange: (String) -> Unit,
    recentGoals: List<FocusGoal>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("专注目标（选填）") },
            placeholder = { Text("例如：复习计算机网络、阅读 20 页") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (recentGoals.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "最近使用的目标",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                recentGoals.forEach { goal ->
                    FilterChip(
                        selected = value == goal.text,
                        onClick = { onValueChange(goal.text) },
                        label = { Text(goal.text) },
                        modifier = Modifier.padding(end = 6.dp, bottom = 4.dp)
                    )
                }
            }
        }
    }
}
