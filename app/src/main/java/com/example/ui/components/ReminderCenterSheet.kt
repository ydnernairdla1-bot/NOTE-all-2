package com.example.ui.components

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.ReminderEntity
import com.example.data.ReminderTargetTab
import com.example.notifications.ReminderNotificationHelper
import com.example.ui.RemindersUiState
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.LocalAppButtonSize
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ReminderHeaderButton(
    activeCount: Int,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val buttonSize = LocalAppButtonSize.current
    val strings = LocalAppStrings.current
    Box(
        modifier = modifier.size(buttonSize.height),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(buttonSize.height)
                .testTag(testTag)
        ) {
            Icon(
                imageVector = if (activeCount > 0) {
                    Icons.Filled.NotificationsActive
                } else {
                    Icons.Outlined.Notifications
                },
                contentDescription = strings.openRemindersDesc,
                tint = if (activeCount > 0) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onBackground
                },
                modifier = Modifier.size(buttonSize.iconSize + 3.dp)
            )
        }
        if (activeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 4.dp)
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
            )
        }
    }
}

private sealed class ActiveTimePickerTarget {
    data class RoutinePreset(val presetId: String, val hour: Int, val minute: Int) : ActiveTimePickerTarget()
    data class CustomComposer(val hour: Int, val minute: Int) : ActiveTimePickerTarget()
    data class ExistingCustom(val reminder: ReminderEntity) : ActiveTimePickerTarget()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderCenterSheet(
    uiState: RemindersUiState,
    onToggleRoutinePreset: (presetId: String, enabled: Boolean) -> Unit,
    onUpdateRoutinePresetTime: (presetId: String, hour: Int, minute: Int) -> Unit,
    onSaveCustomReminder: (
        existingId: String?,
        title: String,
        message: String,
        targetTab: ReminderTargetTab,
        linkedNoteId: String?,
        isDaily: Boolean,
        dateUtcMillis: Long,
        hour: Int,
        minute: Int,
        savedMessageText: String
    ) -> Unit,
    onToggleCustomReminder: (ReminderEntity, Boolean) -> Unit,
    onDeleteReminder: (reminderId: String, deletedMessageText: String) -> Unit,
    onNotifyMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var hasNotifPermission by remember {
        mutableStateOf(ReminderNotificationHelper.hasNotificationPermission(context))
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasNotifPermission = ReminderNotificationHelper.hasNotificationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasNotifPermission = granted || ReminderNotificationHelper.hasNotificationPermission(context)
        }
    )

    fun requestNotificationPermissionIfNeeded() {
        if (!hasNotifPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else if (!hasNotifPermission) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            context.startActivity(intent)
        }
    }

    // Composer state
    var showComposer by remember { mutableStateOf(false) }
    var composerTitle by remember { mutableStateOf("") }
    var composerMessage by remember { mutableStateOf("") }
    var composerTargetTab by remember { mutableStateOf(ReminderTargetTab.NOTES) }
    var composerLinkedNoteId by remember { mutableStateOf<String?>(null) }
    var composerIsDaily by remember { mutableStateOf(false) }
    var composerDateUtcMillis by remember {
        mutableLongStateOf(ReminderEntity.todayUtcMidnightMillis())
    }
    var composerHour by remember {
        mutableIntStateOf((Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 1) % 24)
    }
    var composerMinute by remember { mutableIntStateOf(0) }
    var composerError by remember { mutableStateOf<String?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var timePickerTarget by remember { mutableStateOf<ActiveTimePickerTarget?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.remindersTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = strings.remindersSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = {
                        if (!hasNotifPermission) {
                            requestNotificationPermissionIfNeeded()
                        } else {
                            ReminderNotificationHelper.ensureNotificationChannel(
                                context,
                                strings.notificationChannelName,
                                strings.notificationChannelDesc
                            )
                            ReminderNotificationHelper.sendTestNotification(context, strings)
                            onNotifyMessage(strings.testNotificationSentToast)
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("test_notification_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = strings.testNotificationButton,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // Permission Banner
            if (!hasNotifPermission) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.notifPermissionMissingTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = strings.notifPermissionMissingBody,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { requestNotificationPermissionIfNeeded() },
                                modifier = Modifier.testTag("allow_notifications_button")
                            ) {
                                Text(strings.allowNotificationsAction)
                            }
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = strings.notifPermissionActiveLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Section 1: Daily Routine Presets
            Text(
                text = strings.routineRemindersSection,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            RoutinePresetCard(
                icon = Icons.Outlined.AccountBalanceWallet,
                title = strings.presetFinanceReminderTitle,
                reminder = uiState.financeDailyReminder,
                dailyPrefix = strings.reminderDailyPrefix,
                onToggle = { enabled ->
                    if (enabled && !hasNotifPermission) {
                        requestNotificationPermissionIfNeeded()
                    }
                    onToggleRoutinePreset(ReminderEntity.PRESET_FINANCE_DAILY_ID, enabled)
                },
                onPickTime = {
                    timePickerTarget = ActiveTimePickerTarget.RoutinePreset(
                        presetId = ReminderEntity.PRESET_FINANCE_DAILY_ID,
                        hour = uiState.financeDailyReminder.hour,
                        minute = uiState.financeDailyReminder.minute
                    )
                },
                testTagPrefix = "preset_finance_reminder"
            )

            RoutinePresetCard(
                icon = Icons.Outlined.DirectionsRun,
                title = strings.presetActivitiesReminderTitle,
                reminder = uiState.activitiesDailyReminder,
                dailyPrefix = strings.reminderDailyPrefix,
                onToggle = { enabled ->
                    if (enabled && !hasNotifPermission) {
                        requestNotificationPermissionIfNeeded()
                    }
                    onToggleRoutinePreset(ReminderEntity.PRESET_ACTIVITIES_DAILY_ID, enabled)
                },
                onPickTime = {
                    timePickerTarget = ActiveTimePickerTarget.RoutinePreset(
                        presetId = ReminderEntity.PRESET_ACTIVITIES_DAILY_ID,
                        hour = uiState.activitiesDailyReminder.hour,
                        minute = uiState.activitiesDailyReminder.minute
                    )
                },
                testTagPrefix = "preset_activities_reminder"
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Section 2: Custom & Note Reminders
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.customRemindersSection,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    onClick = {
                        showComposer = !showComposer
                        composerError = null
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.testTag("toggle_add_reminder_composer_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (showComposer) strings.close else strings.addReminderButton,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            AnimatedVisibility(visible = showComposer) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = composerTitle,
                            onValueChange = {
                                composerTitle = it
                                composerLinkedNoteId = null
                                composerError = null
                            },
                            label = { Text(strings.reminderTitleLabel) },
                            placeholder = { Text(strings.reminderTitlePlaceholder) },
                            singleLine = true,
                            isError = composerError != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reminder_title_input")
                        )

                        // Quick pick from recent notes or activities
                        if (uiState.recentNotes.isNotEmpty() || uiState.recentActivities.isNotEmpty()) {
                            Text(
                                text = strings.quickLinkFromNotes,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(uiState.recentNotes, key = { "note_${it.id}" }) { note ->
                                    val label = note.title.ifBlank { note.content.take(24) }
                                    FilterChip(
                                        selected = composerLinkedNoteId == note.id,
                                        onClick = {
                                            composerTitle = label
                                            composerLinkedNoteId = note.id
                                            composerTargetTab = ReminderTargetTab.NOTES
                                            composerError = null
                                        },
                                        label = {
                                            Text(
                                                text = label,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Outlined.Description,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    )
                                }
                                items(uiState.recentActivities, key = { "act_${it.id}" }) { act ->
                                    FilterChip(
                                        selected = composerTitle == act.title && composerTargetTab == ReminderTargetTab.ACTIVITIES,
                                        onClick = {
                                            composerTitle = act.title
                                            composerLinkedNoteId = null
                                            composerTargetTab = ReminderTargetTab.ACTIVITIES
                                            composerIsDaily = true
                                            composerError = null
                                        },
                                        label = {
                                            Text(
                                                text = act.title,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Outlined.DirectionsRun,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = composerMessage,
                            onValueChange = { composerMessage = it },
                            label = { Text(strings.reminderMessageLabel) },
                            placeholder = { Text(strings.reminderMessagePlaceholder) },
                            maxLines = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reminder_message_input")
                        )

                        // Frequency selector: Once vs Daily
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.reminderFrequencyLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FilterChip(
                                selected = !composerIsDaily,
                                onClick = {
                                    composerIsDaily = false
                                    composerError = null
                                },
                                label = { Text(strings.reminderOnceLabel) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            )
                            FilterChip(
                                selected = composerIsDaily,
                                onClick = {
                                    composerIsDaily = true
                                    composerError = null
                                },
                                label = { Text(strings.reminderDailyLabel) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Repeat,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            )
                        }

                        // Date & Time row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!composerIsDaily) {
                                val dateFmt = remember(language) {
                                    SimpleDateFormat("dd MMM yyyy", language.locale)
                                }
                                val previewDate = remember(composerDateUtcMillis, composerHour, composerMinute) {
                                    dateFmt.format(
                                        Date(
                                            ReminderEntity.combineUtcDateAndLocalTime(
                                                composerDateUtcMillis,
                                                composerHour,
                                                composerMinute
                                            )
                                        )
                                    )
                                }
                                OutlinedButton(
                                    onClick = { showDatePicker = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(previewDate, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    timePickerTarget = ActiveTimePickerTarget.CustomComposer(
                                        hour = composerHour,
                                        minute = composerMinute
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(String.format("%02d:%02d", composerHour, composerMinute))
                            }
                        }

                        // Target Tab selector
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(ReminderTargetTab.entries) { tab ->
                                val label = when (tab) {
                                    ReminderTargetTab.NOTES -> strings.tabNotes
                                    ReminderTargetTab.FINANCE -> strings.tabFinance
                                    ReminderTargetTab.ACTIVITIES -> strings.tabActivities
                                }
                                FilterChip(
                                    selected = composerTargetTab == tab,
                                    onClick = {
                                        composerTargetTab = tab
                                        if (tab != ReminderTargetTab.NOTES) {
                                            composerLinkedNoteId = null
                                        }
                                    },
                                    label = { Text(label) }
                                )
                            }
                        }

                        if (composerError != null) {
                            Text(
                                text = composerError.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Button(
                            onClick = {
                                val trimmedTitle = composerTitle.trim()
                                if (trimmedTitle.isEmpty()) {
                                    composerError = strings.reminderTitleRequiredError
                                    return@Button
                                }
                                if (!composerIsDaily) {
                                    val candidateTrigger = ReminderEntity.combineUtcDateAndLocalTime(
                                        composerDateUtcMillis,
                                        composerHour,
                                        composerMinute
                                    )
                                    if (candidateTrigger <= System.currentTimeMillis()) {
                                        composerError = strings.reminderPastTimeError
                                        return@Button
                                    }
                                }
                                if (!hasNotifPermission) {
                                    requestNotificationPermissionIfNeeded()
                                }
                                val timeText = String.format("%02d:%02d", composerHour, composerMinute)
                                onSaveCustomReminder(
                                    null,
                                    trimmedTitle,
                                    composerMessage,
                                    composerTargetTab,
                                    composerLinkedNoteId,
                                    composerIsDaily,
                                    composerDateUtcMillis,
                                    composerHour,
                                    composerMinute,
                                    strings.reminderSavedMessage(timeText)
                                )
                                composerTitle = ""
                                composerMessage = ""
                                composerLinkedNoteId = null
                                composerError = null
                                showComposer = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_reminder_button")
                        ) {
                            Text(strings.saveReminderAction)
                        }
                    }
                }
            }

            if (uiState.customReminders.isEmpty()) {
                Text(
                    text = strings.noCustomRemindersYet,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.customReminders.forEach { reminder ->
                        CustomReminderRow(
                            reminder = reminder,
                            onToggle = { enabled ->
                                if (enabled && !hasNotifPermission) {
                                    requestNotificationPermissionIfNeeded()
                                }
                                onToggleCustomReminder(reminder, enabled)
                            },
                            onPickTime = {
                                timePickerTarget = ActiveTimePickerTarget.ExistingCustom(reminder)
                            },
                            onDelete = {
                                onDeleteReminder(reminder.id, strings.reminderDeletedMessage)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("close_reminders_button")
            ) {
                Text(strings.close)
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = composerDateUtcMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            composerDateUtcMillis = it
                            composerError = null
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(strings.confirmAction)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(strings.cancel)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val currentPickerTarget = timePickerTarget
    if (currentPickerTarget != null) {
        val initHour = when (currentPickerTarget) {
            is ActiveTimePickerTarget.RoutinePreset -> currentPickerTarget.hour
            is ActiveTimePickerTarget.CustomComposer -> currentPickerTarget.hour
            is ActiveTimePickerTarget.ExistingCustom -> currentPickerTarget.reminder.hour
        }
        val initMinute = when (currentPickerTarget) {
            is ActiveTimePickerTarget.RoutinePreset -> currentPickerTarget.minute
            is ActiveTimePickerTarget.CustomComposer -> currentPickerTarget.minute
            is ActiveTimePickerTarget.ExistingCustom -> currentPickerTarget.reminder.minute
        }
        val timePickerState = rememberTimePickerState(
            initialHour = initHour,
            initialMinute = initMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { timePickerTarget = null },
            title = { Text(strings.pickTimeDialogTitle) },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val h = timePickerState.hour
                        val m = timePickerState.minute
                        when (currentPickerTarget) {
                            is ActiveTimePickerTarget.RoutinePreset -> {
                                if (!hasNotifPermission) {
                                    requestNotificationPermissionIfNeeded()
                                }
                                onUpdateRoutinePresetTime(currentPickerTarget.presetId, h, m)
                            }
                            is ActiveTimePickerTarget.CustomComposer -> {
                                composerHour = h
                                composerMinute = m
                                composerError = null
                            }
                            is ActiveTimePickerTarget.ExistingCustom -> {
                                val rem = currentPickerTarget.reminder
                                val timeText = String.format("%02d:%02d", h, m)
                                val baseDateUtc = ReminderEntity.todayUtcMidnightMillis(rem.triggerAtMillis)
                                val nextTriggerIfOnce = ReminderEntity.combineUtcDateAndLocalTime(baseDateUtc, h, m)
                                val finalDateUtc = if (!rem.isDaily && nextTriggerIfOnce <= System.currentTimeMillis()) {
                                    ReminderEntity.todayUtcMidnightMillis(System.currentTimeMillis() + 86_400_000L)
                                } else {
                                    baseDateUtc
                                }
                                onSaveCustomReminder(
                                    rem.id,
                                    rem.title,
                                    rem.message,
                                    rem.targetTabEnum,
                                    rem.linkedNoteId,
                                    rem.isDaily,
                                    finalDateUtc,
                                    h,
                                    m,
                                    strings.reminderSavedMessage(timeText)
                                )
                            }
                        }
                        timePickerTarget = null
                    }
                ) {
                    Text(strings.confirmAction)
                }
            },
            dismissButton = {
                TextButton(onClick = { timePickerTarget = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
private fun RoutinePresetCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    reminder: ReminderEntity,
    dailyPrefix: String,
    onToggle: (Boolean) -> Unit,
    onPickTime: () -> Unit,
    testTagPrefix: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (reminder.isEnabled) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        border = BorderStroke(
            1.dp,
            if (reminder.isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (reminder.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable(onClick = onPickTime)
                        .testTag("${testTagPrefix}_time")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Alarm,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "$dailyPrefix • ${reminder.formattedTime()}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            Switch(
                checked = reminder.isEnabled,
                onCheckedChange = onToggle,
                modifier = Modifier.testTag("${testTagPrefix}_switch")
            )
        }
    }
}

@Composable
private fun CustomReminderRow(
    reminder: ReminderEntity,
    onToggle: (Boolean) -> Unit,
    onPickTime: () -> Unit,
    onDelete: () -> Unit
) {
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current
    val dateFmt = remember(language) { SimpleDateFormat("dd MMM, HH:mm", language.locale) }
    val scheduleText = if (reminder.isDaily) {
        "${strings.reminderDailyPrefix} • ${reminder.formattedTime()}"
    } else if (!reminder.isEnabled && reminder.triggerAtMillis <= System.currentTimeMillis()) {
        "${strings.reminderOncePrefix} • ${strings.reminderStatusTriggered}"
    } else {
        "${strings.reminderOncePrefix} • ${dateFmt.format(Date(reminder.triggerAtMillis))}"
    }
    val tabLabel = when (reminder.targetTabEnum) {
        ReminderTargetTab.NOTES -> strings.tabNotes
        ReminderTargetTab.FINANCE -> strings.tabFinance
        ReminderTargetTab.ACTIVITIES -> strings.tabActivities
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onPickTime),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (reminder.message.isNotBlank()) {
                    Text(
                        text = reminder.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "$scheduleText • $tabLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (reminder.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = reminder.isEnabled,
                onCheckedChange = onToggle
            )

            IconButton(
                onClick = onDelete
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = strings.deleteReminderDesc,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
