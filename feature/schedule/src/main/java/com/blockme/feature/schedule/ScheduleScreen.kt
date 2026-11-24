package com.blockme.feature.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blockme.core.common.formatTimeWithAmPm
import com.blockme.core.common.toShortDurationString
import com.blockme.core.domain.model.FocusSchedule
import com.blockme.core.domain.model.RepeatType

/**
 * Material 3 Schedules Screen with Analog Clock TimePicker.
 *
 * SPDX-License-Identifier: MIT
 */
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val schedules by viewModel.schedules.collectAsStateWithLifecycle()
    val showAddDialog by viewModel.showAddDialog.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                text = "定时计划",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "按计划自动启动专注锁定",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (schedules.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Text(
                            text = "暂无重复计划",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "点击下方的 + 按钮，创建第一条自动专注计划。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(schedules, key = { it.id }) { schedule ->
                        ScheduleCard(
                            schedule = schedule,
                            onToggle = { enabled ->
                                viewModel.toggleSchedule(schedule.id, enabled)
                            },
                            onDelete = { viewModel.deleteSchedule(schedule.id) }
                        )
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = viewModel::showAddScheduleDialog,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "添加计划")
        }
    }

    if (showAddDialog) {
        AddScheduleDialog(
            onDismiss = viewModel::hideAddScheduleDialog,
            onConfirm = viewModel::addSchedule
        )
    }
}

@Composable
private fun ScheduleCard(
    schedule: FocusSchedule,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (schedule.enabled) {
                MaterialTheme.colorScheme.surfaceContainerLow
            } else {
                MaterialTheme.colorScheme.surfaceContainerLowest
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatTimeWithAmPm(schedule.hourOfDay, schedule.minuteOfHour),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (schedule.enabled) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${schedule.durationMs.toShortDurationString()} · ${schedule.formattedDays()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (schedule.goalText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = schedule.goalText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = schedule.enabled,
                    onCheckedChange = onToggle
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "删除计划",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private val DAYS_OF_WEEK = listOf(
    java.util.Calendar.MONDAY to "周一",
    java.util.Calendar.TUESDAY to "周二",
    java.util.Calendar.WEDNESDAY to "周三",
    java.util.Calendar.THURSDAY to "周四",
    java.util.Calendar.FRIDAY to "周五",
    java.util.Calendar.SATURDAY to "周六",
    java.util.Calendar.SUNDAY to "周日"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddScheduleDialog(
    onDismiss: () -> Unit,
    onConfirm: (FocusSchedule) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = 9,
        initialMinute = 0,
        is24Hour = false
    )
    var durationMs by remember { mutableLongStateOf(30 * 60_000L) }
    var selectedDays by remember {
        mutableStateOf(
            setOf(
                java.util.Calendar.MONDAY,
                java.util.Calendar.TUESDAY,
                java.util.Calendar.WEDNESDAY,
                java.util.Calendar.THURSDAY,
                java.util.Calendar.FRIDAY,
                java.util.Calendar.SATURDAY,
                java.util.Calendar.SUNDAY
            )
        )
    }
    var goalText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "新建专注计划",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "选择开始时间",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Analog Material 3 Clock TimePicker
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        selectorColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Duration Selector
                Text(
                    text = "专注时长",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 30, 45, 60, 90, 120, 180).forEach { min ->
                        val ms = min * 60_000L
                        val label = if (min >= 60) "${min / 60}小时${if (min % 60 != 0) "${min % 60}分钟" else ""}" else "${min}分钟"
                        FilterChip(
                            selected = durationMs == ms,
                            onClick = { durationMs = ms },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Repeat Days Selection (All 7 Days)
                Text(
                    text = "重复日期",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DAYS_OF_WEEK.forEach { (day, label) ->
                        val isSelected = selectedDays.contains(day)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedDays = if (isSelected) {
                                    if (selectedDays.size > 1) selectedDays - day else selectedDays
                                } else {
                                    selectedDays + day
                                }
                            },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Focus Goal Input
                OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it },
                    label = { Text("目标 / 标题（选填）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val isAll7 = selectedDays.size == 7
                    onConfirm(
                        FocusSchedule(
                            id = 0,
                            label = if (goalText.isNotBlank()) goalText else "定时专注",
                            hourOfDay = timePickerState.hour,
                            minuteOfHour = timePickerState.minute,
                            durationMs = durationMs,
                            repeatType = if (isAll7) RepeatType.DAILY else RepeatType.CUSTOM,
                            customDays = selectedDays,
                            enabled = true,
                            goalText = goalText
                        )
                    )
                }
            ) {
                Text("保存计划")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

fun FocusSchedule.formattedDays(): String {
    val days = if (customDays.isNotEmpty()) {
        customDays
    } else {
        when (repeatType) {
            RepeatType.WEEKDAYS -> setOf(
                java.util.Calendar.MONDAY,
                java.util.Calendar.TUESDAY,
                java.util.Calendar.WEDNESDAY,
                java.util.Calendar.THURSDAY,
                java.util.Calendar.FRIDAY
            )
            RepeatType.WEEKENDS -> setOf(
                java.util.Calendar.SATURDAY,
                java.util.Calendar.SUNDAY
            )
            else -> setOf(
                java.util.Calendar.SUNDAY,
                java.util.Calendar.MONDAY,
                java.util.Calendar.TUESDAY,
                java.util.Calendar.WEDNESDAY,
                java.util.Calendar.THURSDAY,
                java.util.Calendar.FRIDAY,
                java.util.Calendar.SATURDAY
            )
        }
    }

    val all7 = setOf(
        java.util.Calendar.SUNDAY,
        java.util.Calendar.MONDAY,
        java.util.Calendar.TUESDAY,
        java.util.Calendar.WEDNESDAY,
        java.util.Calendar.THURSDAY,
        java.util.Calendar.FRIDAY,
        java.util.Calendar.SATURDAY
    )
    val weekdays = setOf(
        java.util.Calendar.MONDAY,
        java.util.Calendar.TUESDAY,
        java.util.Calendar.WEDNESDAY,
        java.util.Calendar.THURSDAY,
        java.util.Calendar.FRIDAY
    )
    val weekends = setOf(
        java.util.Calendar.SATURDAY,
        java.util.Calendar.SUNDAY
    )

    return when {
        days.containsAll(all7) -> "每天"
        days == weekdays -> "周一至周五"
        days == weekends -> "周末"
        else -> {
            DAYS_OF_WEEK
                .filter { days.contains(it.first) }
                .joinToString(", ") { it.second }
        }
    }
}

