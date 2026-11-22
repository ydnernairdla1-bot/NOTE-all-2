package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.AppDatabase
import com.example.data.BackupArchiveManager
import com.example.data.ImageStorageManager
import com.example.data.NoteRepository
import com.example.data.PdfExportManager
import com.example.data.PreferencesStore
import com.example.data.ReminderTargetTab
import com.example.notifications.ReminderNotificationHelper
import com.example.ui.ActivitiesViewModel
import com.example.ui.AppearanceViewModel
import com.example.ui.FinanceViewModel
import com.example.ui.NoteEditorViewModel
import com.example.ui.NotesViewModel
import com.example.ui.RemindersViewModel
import com.example.ui.components.AppBackgroundContainer
import com.example.ui.components.ReminderCenterSheet
import com.example.ui.components.ThemeCustomizerSheet
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.screens.ActivitiesScreen
import com.example.ui.screens.BackupCenterScreen
import com.example.ui.screens.FinanceScreen
import com.example.ui.screens.NoteEditorScreen
import com.example.ui.screens.NotesHomeScreen
import com.example.ui.theme.CloudNoteTheme

private const val ROUTE_MAIN_TABS = "main_tabs"
private const val ROUTE_NOTE_EDITOR = "note_editor/{noteId}"
private const val ROUTE_BACKUP_CENTER = "backup_center"

private enum class MainTab {
    NOTES,
    FINANCE,
    ACTIVITIES
}

private data class PendingNotificationNavigation(
    val targetTab: ReminderTargetTab,
    val linkedNoteId: String?
)

class MainActivity : ComponentActivity() {
    private val pendingNotificationNav = mutableStateOf<PendingNotificationNavigation?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleNotificationIntent(intent)
        setContent {
            CloudNoteApp(
                pendingNavigation = pendingNotificationNav.value,
                onConsumePendingNavigation = { pendingNotificationNav.value = null }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent?.action == ReminderNotificationHelper.ACTION_OPEN_REMINDER_TARGET) {
            val tabKey = intent.getStringExtra(ReminderNotificationHelper.EXTRA_TARGET_TAB)
                ?: ReminderTargetTab.NOTES.key
            val noteId = intent.getStringExtra(ReminderNotificationHelper.EXTRA_NOTE_ID)
            pendingNotificationNav.value = PendingNotificationNavigation(
                targetTab = ReminderTargetTab.fromKey(tabKey),
                linkedNoteId = noteId
            )
        }
    }
}

@Composable
private fun CloudNoteApp(
    pendingNavigation: PendingNotificationNavigation?,
    onConsumePendingNavigation: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val appDependencies = remember(context) {
        val db = AppDatabase.getInstance(context)
        val imageManager = ImageStorageManager(context)
        val backupManager = BackupArchiveManager(context, imageManager)
        val pdfExportManager = PdfExportManager(context, imageManager)
        val prefs = PreferencesStore(context)
        val repository = NoteRepository(
            noteDao = db.noteDao(),
            noteCategoryDao = db.noteCategoryDao(),
            transactionDao = db.transactionDao(),
            activityDao = db.activityDao(),
            reminderDao = db.reminderDao(),
            imageStorageManager = imageManager,
            backupArchiveManager = backupManager,
            pdfExportManager = pdfExportManager,
            preferencesStore = prefs
        )
        Triple(repository, prefs, imageManager)
    }
    val (repository, prefsStore, imageManager) = appDependencies

    val appearanceViewModel: AppearanceViewModel = viewModel(
        factory = AppearanceViewModel.factory(prefsStore, imageManager)
    )
    val appearanceSettings by appearanceViewModel.appearanceState.collectAsStateWithLifecycle()

    val notesViewModel: NotesViewModel = viewModel(
        factory = NotesViewModel.factory(repository)
    )
    val financeViewModel: FinanceViewModel = viewModel(
        factory = FinanceViewModel.factory(repository)
    )
    val activitiesViewModel: ActivitiesViewModel = viewModel(
        factory = ActivitiesViewModel.factory(repository)
    )
    val remindersViewModel: RemindersViewModel = viewModel(
        factory = RemindersViewModel.factory(context, repository)
    )

    val homeState by notesViewModel.uiState.collectAsStateWithLifecycle()
    val financeState by financeViewModel.uiState.collectAsStateWithLifecycle()
    val activitiesState by activitiesViewModel.uiState.collectAsStateWithLifecycle()
    val remindersState by remindersViewModel.uiState.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    var showThemeCustomizer by rememberSaveable { mutableStateOf(false) }
    var showRemindersSheet by rememberSaveable { mutableStateOf(false) }

    val backgroundIsDark: Boolean? = when (appearanceSettings.backgroundMode) {
        com.example.ui.theme.BackgroundMode.GRADIENT -> appearanceSettings.gradientPreset.isDarkBackground
        com.example.ui.theme.BackgroundMode.SOLID -> appearanceSettings.solidPreset.isDarkBackground
        else -> null
    }

    CloudNoteTheme(
        themeMode = appearanceSettings.themeMode,
        themePreset = appearanceSettings.themePreset,
        fontChoice = appearanceSettings.fontChoice,
        fontScale = appearanceSettings.fontScale,
        buttonSize = appearanceSettings.buttonSize,
        language = appearanceSettings.language,
        backgroundIsDark = backgroundIsDark
    ) {
        val strings = LocalAppStrings.current
        LaunchedEffect(context, strings.notificationChannelName) {
            ReminderNotificationHelper.ensureNotificationChannel(
                context = context,
                channelName = strings.notificationChannelName,
                channelDesc = strings.notificationChannelDesc
            )
        }

        NavHost(
            navController = navController,
            startDestination = ROUTE_MAIN_TABS
        ) {
            composable(
                route = ROUTE_MAIN_TABS,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                var activeTab by rememberSaveable { mutableStateOf(MainTab.NOTES) }
                var showAddTransactionSheet by rememberSaveable { mutableStateOf(false) }
                var showAddActivitySheet by rememberSaveable { mutableStateOf(false) }
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(pendingNavigation) {
                    val navTarget = pendingNavigation ?: return@LaunchedEffect
                    activeTab = when (navTarget.targetTab) {
                        ReminderTargetTab.NOTES -> MainTab.NOTES
                        ReminderTargetTab.FINANCE -> MainTab.FINANCE
                        ReminderTargetTab.ACTIVITIES -> MainTab.ACTIVITIES
                    }
                    val noteId = navTarget.linkedNoteId
                    if (!noteId.isNullOrBlank()) {
                        navController.navigate("note_editor/$noteId")
                    }
                    onConsumePendingNavigation()
                }

                LaunchedEffect(remindersState.statusMessage) {
                    val message = remindersState.statusMessage
                    if (!message.isNullOrBlank()) {
                        snackbarHostState.showSnackbar(message)
                        remindersViewModel.clearStatusMessage()
                    }
                }

                AppBackgroundContainer(
                    settings = appearanceSettings,
                    imageStorageManager = imageManager
                ) {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val useNavigationRail = maxWidth >= 600.dp

                        Scaffold(
                            contentWindowInsets = WindowInsets.safeDrawing,
                            containerColor = Color.Transparent,
                            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                            bottomBar = {
                                if (!useNavigationRail) {
                                    NavigationBar(
                                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                                        tonalElevation = 3.dp
                                    ) {
                                        MainTab.entries.forEach { tab ->
                                            val selected = activeTab == tab
                                            val tabLabel = when (tab) {
                                                MainTab.NOTES -> strings.tabNotes
                                                MainTab.FINANCE -> strings.tabFinance
                                                MainTab.ACTIVITIES -> strings.tabActivities
                                            }
                                            NavigationBarItem(
                                                selected = selected,
                                                onClick = { activeTab = tab },
                                                icon = {
                                                    Icon(
                                                        imageVector = when (tab) {
                                                            MainTab.NOTES -> if (selected) Icons.Filled.Description else Icons.Outlined.Description
                                                            MainTab.FINANCE -> if (selected) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet
                                                            MainTab.ACTIVITIES -> if (selected) Icons.Filled.DirectionsRun else Icons.Outlined.DirectionsRun
                                                        },
                                                        contentDescription = tabLabel
                                                    )
                                                },
                                                label = { Text(tabLabel) },
                                                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                            )
                                        }
                                    }
                                }
                            },
                            floatingActionButton = {
                                val btnSize = appearanceSettings.buttonSize
                                when (activeTab) {
                                    MainTab.NOTES -> {
                                        Surface(
                                            onClick = {
                                                notesViewModel.createNewNote { newId ->
                                                    navController.navigate("note_editor/$newId")
                                                }
                                            },
                                            color = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary,
                                            shape = RoundedCornerShape(12.dp),
                                            shadowElevation = 4.dp,
                                            modifier = Modifier
                                                .size(btnSize.fabSize)
                                                .testTag("fab_new_note")
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Filled.Add,
                                                    contentDescription = strings.newNoteAction,
                                                    modifier = Modifier.size(btnSize.iconSize + 4.dp)
                                                )
                                            }
                                        }
                                    }
                                    MainTab.FINANCE -> {
                                        Surface(
                                            onClick = { showAddTransactionSheet = true },
                                            color = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary,
                                            shape = RoundedCornerShape(12.dp),
                                            shadowElevation = 4.dp,
                                            modifier = Modifier
                                                .size(btnSize.fabSize)
                                                .testTag("fab_add_transaction")
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Filled.Add,
                                                    contentDescription = strings.addTransactionAction,
                                                    modifier = Modifier.size(btnSize.iconSize + 4.dp)
                                                )
                                            }
                                        }
                                    }
                                    MainTab.ACTIVITIES -> {
                                        Surface(
                                            onClick = { showAddActivitySheet = true },
                                            color = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary,
                                            shape = RoundedCornerShape(12.dp),
                                            shadowElevation = 4.dp,
                                            modifier = Modifier
                                                .size(btnSize.fabSize)
                                                .testTag("fab_add_activity")
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Filled.Add,
                                                    contentDescription = strings.addActivityAction,
                                                    modifier = Modifier.size(btnSize.iconSize + 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        ) { innerPadding ->
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                if (useNavigationRail) {
                                    NavigationRail(
                                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
                                    ) {
                                        Spacer(modifier = Modifier.weight(1f))
                                        MainTab.entries.forEach { tab ->
                                            val selected = activeTab == tab
                                            val tabLabel = when (tab) {
                                                MainTab.NOTES -> strings.tabNotes
                                                MainTab.FINANCE -> strings.tabFinance
                                                MainTab.ACTIVITIES -> strings.tabActivities
                                            }
                                            NavigationRailItem(
                                                selected = selected,
                                                onClick = { activeTab = tab },
                                                icon = {
                                                    Icon(
                                                        imageVector = when (tab) {
                                                            MainTab.NOTES -> if (selected) Icons.Filled.Description else Icons.Outlined.Description
                                                            MainTab.FINANCE -> if (selected) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet
                                                            MainTab.ACTIVITIES -> if (selected) Icons.Filled.DirectionsRun else Icons.Outlined.DirectionsRun
                                                        },
                                                        contentDescription = tabLabel
                                                    )
                                                },
                                                label = { Text(tabLabel) },
                                                modifier = Modifier.testTag("rail_tab_${tab.name.lowercase()}")
                                            )
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }

                                when (activeTab) {
                                    MainTab.NOTES -> {
                                        NotesHomeScreen(
                                            uiState = homeState,
                                            activeRemindersCount = remindersState.activeCount,
                                            imageStorageManager = notesViewModel.imageStorageManager,
                                            snackbarHostState = snackbarHostState,
                                            onSearchQueryChange = notesViewModel::onSearchQueryChange,
                                            onFilterSelected = notesViewModel::onFilterSelected,
                                            onCategorySelected = notesViewModel::onCategorySelected,
                                            onCreateCategory = notesViewModel::createCategory,
                                            onRenameCategory = notesViewModel::renameCategory,
                                            onDeleteCategory = notesViewModel::deleteCategory,
                                            onToggleGridView = notesViewModel::toggleGridView,
                                            onOpenNote = { noteId ->
                                                navController.navigate("note_editor/$noteId")
                                            },
                                            onCreateNewNote = {
                                                notesViewModel.createNewNote { newId ->
                                                    navController.navigate("note_editor/$newId")
                                                }
                                            },
                                            onTogglePin = notesViewModel::togglePin,
                                            onDeleteNote = notesViewModel::deleteNote,
                                            onOpenBackupCenter = {
                                                notesViewModel.refreshStorageStats()
                                                navController.navigate(ROUTE_BACKUP_CENTER)
                                            },
                                            onOpenReminders = { showRemindersSheet = true },
                                            onOpenThemeCustomizer = { showThemeCustomizer = true },
                                            onDismissLocalNotice = notesViewModel::dismissLocalBanner,
                                            onClearStatusMessage = notesViewModel::clearStatusMessage,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    MainTab.FINANCE -> {
                                        FinanceScreen(
                                            uiState = financeState,
                                            pendingBackupCount = homeState.pendingBackupCount,
                                            activeRemindersCount = remindersState.activeCount,
                                            imageStorageManager = financeViewModel.imageStorageManager,
                                            snackbarHostState = snackbarHostState,
                                            showTransactionSheet = showAddTransactionSheet,
                                            onOpenAddTransaction = { showAddTransactionSheet = true },
                                            onDismissTransactionSheet = { showAddTransactionSheet = false },
                                            onPeriodSelected = financeViewModel::onPeriodSelected,
                                            onTypeFilterSelected = financeViewModel::onTypeFilterSelected,
                                            onSearchQueryChange = financeViewModel::onSearchQueryChange,
                                            onSaveReceiptFromUri = financeViewModel::saveReceiptImage,
                                            onDeleteUnusedReceipt = financeViewModel::deleteUnusedReceipt,
                                            onSaveTransaction = { id, type, amount, title, category, note, receiptName, date ->
                                                financeViewModel.saveTransaction(
                                                    existingId = id,
                                                    type = type,
                                                    amount = amount,
                                                    title = title,
                                                    category = category,
                                                    note = note,
                                                    receiptImageName = receiptName,
                                                    transactionDate = date,
                                                    onComplete = { notesViewModel.refreshStorageStats() }
                                                )
                                            },
                                            onDeleteTransaction = { tx ->
                                                financeViewModel.deleteTransaction(tx)
                                                notesViewModel.refreshStorageStats()
                                            },
                                            onOpenBackupCenter = {
                                                notesViewModel.refreshStorageStats()
                                                navController.navigate(ROUTE_BACKUP_CENTER)
                                            },
                                            onOpenReminders = { showRemindersSheet = true },
                                            onOpenThemeCustomizer = { showThemeCustomizer = true },
                                            onClearStatusMessage = financeViewModel::clearStatusMessage,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    MainTab.ACTIVITIES -> {
                                        ActivitiesScreen(
                                            uiState = activitiesState,
                                            pendingBackupCount = homeState.pendingBackupCount,
                                            activeRemindersCount = remindersState.activeCount,
                                            imageStorageManager = activitiesViewModel.imageStorageManager,
                                            snackbarHostState = snackbarHostState,
                                            showActivitySheet = showAddActivitySheet,
                                            onOpenAddActivity = { showAddActivitySheet = true },
                                            onDismissActivitySheet = { showAddActivitySheet = false },
                                            onStatusFilterSelected = activitiesViewModel::onStatusFilterSelected,
                                            onCategorySelected = activitiesViewModel::onCategorySelected,
                                            onToggleCompletedToday = activitiesViewModel::toggleCompletedToday,
                                            onAddQuickPreset = activitiesViewModel::addQuickPreset,
                                            onSavePhotoFromUri = activitiesViewModel::saveActivityPhoto,
                                            onDeleteUnusedPhoto = activitiesViewModel::deleteUnusedPhoto,
                                            onSaveActivity = { id, title, category, mins, note, photoName, markDone ->
                                                activitiesViewModel.saveActivity(
                                                    existingId = id,
                                                    title = title,
                                                    category = category,
                                                    targetMinutes = mins,
                                                    note = note,
                                                    photoImageName = photoName,
                                                    markCompletedToday = markDone,
                                                    onComplete = { notesViewModel.refreshStorageStats() }
                                                )
                                            },
                                            onDeleteActivity = { act ->
                                                activitiesViewModel.deleteActivity(act)
                                                notesViewModel.refreshStorageStats()
                                            },
                                            onOpenBackupCenter = {
                                                notesViewModel.refreshStorageStats()
                                                navController.navigate(ROUTE_BACKUP_CENTER)
                                            },
                                            onOpenReminders = { showRemindersSheet = true },
                                            onOpenThemeCustomizer = { showThemeCustomizer = true },
                                            onClearStatusMessage = activitiesViewModel::clearStatusMessage,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            composable(
                route = ROUTE_NOTE_EDITOR,
                arguments = listOf(navArgument("noteId") { type = NavType.StringType }),
                enterTransition = { slideInHorizontally { it / 4 } + fadeIn() },
                exitTransition = { slideOutHorizontally { it / 4 } + fadeOut() }
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getString("noteId").orEmpty()
                val editorViewModel: NoteEditorViewModel = viewModel(
                    key = "editor_$noteId",
                    factory = NoteEditorViewModel.factory(noteId, repository)
                )
                val editorState by editorViewModel.uiState.collectAsStateWithLifecycle()

                NoteEditorScreen(
                    uiState = editorState,
                    imageStorageManager = editorViewModel.imageStorageManager,
                    onTitleChange = editorViewModel::onTitleChange,
                    onContentChange = editorViewModel::onContentChange,
                    onCategoryChange = editorViewModel::onCategoryChange,
                    onCreateCategory = editorViewModel::createAndSelectCategory,
                    onColorChange = editorViewModel::onColorChange,
                    onTogglePin = editorViewModel::onTogglePin,
                    onAddImagesFromUris = { uris ->
                        editorViewModel.addImagesFromUris(uris)
                        notesViewModel.refreshStorageStats()
                    },
                    onAddImageFromCameraFile = { file ->
                        editorViewModel.addImageFromCameraFile(file)
                        notesViewModel.refreshStorageStats()
                    },
                    onRemoveImage = { fileName ->
                        editorViewModel.removeImage(fileName)
                        notesViewModel.refreshStorageStats()
                    },
                    onDeleteNote = {
                        editorViewModel.deleteNote {
                            notesViewModel.refreshStorageStats()
                            navController.popBackStack()
                        }
                    },
                    onBackPressed = {
                        editorViewModel.flushAndExit {
                            notesViewModel.refreshStorageStats()
                            navController.popBackStack()
                        }
                    },
                    onExportPdf = editorViewModel::exportNoteToPdf,
                    onClearStatusMessage = editorViewModel::clearStatusMessage,
                    onClearError = editorViewModel::clearError
                )
            }

            composable(
                route = ROUTE_BACKUP_CENTER,
                enterTransition = { slideInHorizontally { it / 4 } + fadeIn() },
                exitTransition = { slideOutHorizontally { it / 4 } + fadeOut() }
            ) {
                AppBackgroundContainer(
                    settings = appearanceSettings,
                    imageStorageManager = imageManager
                ) {
                    BackupCenterScreen(
                        uiState = homeState,
                        onExportPdf = notesViewModel::exportPdf,
                        onExportBackup = notesViewModel::exportBackup,
                        onImportBackup = notesViewModel::importBackup,
                        onClearStatusMessage = notesViewModel::clearStatusMessage,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }

        if (showRemindersSheet) {
            ReminderCenterSheet(
                uiState = remindersState,
                onToggleRoutinePreset = remindersViewModel::toggleRoutinePreset,
                onUpdateRoutinePresetTime = remindersViewModel::updateRoutinePresetTime,
                onSaveCustomReminder = remindersViewModel::saveCustomReminder,
                onToggleCustomReminder = remindersViewModel::toggleCustomReminder,
                onDeleteReminder = remindersViewModel::deleteReminder,
                onNotifyMessage = remindersViewModel::setStatusMessage,
                onDismiss = { showRemindersSheet = false }
            )
        }

        if (showThemeCustomizer) {
            ThemeCustomizerSheet(
                settings = appearanceSettings,
                imageStorageManager = imageManager,
                onSelectLanguage = appearanceViewModel::setLanguage,
                onSelectThemeMode = appearanceViewModel::setThemeMode,
                onSelectThemePreset = appearanceViewModel::setThemePreset,
                onSelectFontChoice = appearanceViewModel::setFontChoice,
                onSelectFontScale = appearanceViewModel::setFontScale,
                onSelectButtonSize = appearanceViewModel::setButtonSize,
                onSelectBackgroundMode = appearanceViewModel::setBackgroundMode,
                onSelectGradient = appearanceViewModel::setGradientPreset,
                onSelectSolid = appearanceViewModel::setSolidPreset,
                onSelectCustomPhoto = { uri ->
                    appearanceViewModel.setCustomBackgroundPhoto(uri)
                    notesViewModel.refreshStorageStats()
                },
                onChangeImageDim = appearanceViewModel::setImageDimAlpha,
                onResetDefault = {
                    appearanceViewModel.resetToDefault()
                    notesViewModel.refreshStorageStats()
                },
                onDismiss = { showThemeCustomizer = false }
            )
        }
    }
}
