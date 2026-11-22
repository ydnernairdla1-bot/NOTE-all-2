package com.example.ui.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.data.TransactionType
import com.example.ui.ActivityStatusFilter
import com.example.ui.FinancePeriod
import com.example.ui.FinanceTypeFilter
import com.example.ui.NotesFilter
import com.example.ui.theme.AppButtonSize
import com.example.ui.theme.AppFontChoice
import com.example.ui.theme.AppFontScale
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.BackgroundGradientPreset
import com.example.ui.theme.BackgroundMode
import com.example.ui.theme.BackgroundSolidPreset
import java.util.Locale

enum class AppLanguage(
    val key: String,
    val nativeLabel: String,
    val locale: Locale
) {
    INDONESIAN("ID", "Indonesia", Locale("id", "ID")),
    ENGLISH("EN", "English", Locale.ENGLISH),
    RUSSIAN("RU", "Русский", Locale("ru", "RU")),
    MANDARIN("ZH", "中文", Locale.SIMPLIFIED_CHINESE);

    companion object {
        fun fromKey(key: String): AppLanguage =
            entries.firstOrNull { it.key == key } ?: INDONESIAN
    }
}

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.INDONESIAN }
val LocalAppStrings = staticCompositionLocalOf { indonesianStrings() }

fun stringsForLanguage(language: AppLanguage): AppStrings = when (language) {
    AppLanguage.INDONESIAN -> indonesianStrings()
    AppLanguage.ENGLISH -> englishStrings()
    AppLanguage.RUSSIAN -> russianStrings()
    AppLanguage.MANDARIN -> mandarinStrings()
}

data class AppStrings(
    // Tabs
    val tabNotes: String,
    val tabFinance: String,
    val tabActivities: String,

    // Relative time
    val justNow: String,
    val minutesAgo: (Long) -> String,
    val hoursAgo: (Long) -> String,
    val yesterday: String,

    // Common actions
    val cancel: String,
    val delete: String,
    val saveChanges: String,
    val select: String,
    val close: String,
    val reset: String,
    val showAll: String,
    val clearSearch: String,
    val back: String,
    val pdfChip: String,
    val exportPdfShort: String,
    val openSettingsDesc: String,

    // Notes Home
    val notesStatsSummary: (Int, Int) -> String,
    val localNoticeCompact: String,
    val searchNotesHint: String,
    val toggleToList: String,
    val toggleToGrid: String,
    val emptyNotesTitle: String,
    val emptyNotesSubtitle: String,
    val emptySearchTitle: String,
    val emptySearchSubtitle: String,
    val createNoteButton: String,
    val newNoteAction: String,
    val deleteNoteConfirmTitle: String,
    val deleteNoteConfirmBody: String,
    val photoNoteFallback: String,
    val statusBackedUp: String,
    val statusLocal: String,
    val pinNote: String,
    val unpinNote: String,
    val deleteNoteAction: String,

    // Note Editor
    val autoSaved: String,
    val saving: String,
    val editorTitleHint: String,
    val editorBodyHint: String,
    val gallery: String,
    val camera: String,
    val photoCount: (Int) -> String,
    val shareNote: String,
    val exportNotePdfDesc: String,
    val removeImageDesc: String,
    val fullscreenPhotoDesc: (Int) -> String,
    val deleteImageDesc: String,

    // Finance
    val financeTitle: String,
    val cashFlowPrefix: String,
    val netBalanceTitle: String,
    val txCountLabel: (Int) -> String,
    val incomeLabel: String,
    val expenseLabel: String,
    val expensePerCategory: String,
    val searchFinanceHint: String,
    val financeEmptyTitle: String,
    val financeEmptySubtitle: String,
    val addTransactionAction: String,
    val editTransactionTitle: String,
    val deleteTxConfirmTitle: String,
    val deleteTxConfirmBody: (String) -> String,
    val amountLabel: String,
    val amountReadAs: (String) -> String,
    val txTitleLabel: String,
    val txExpensePlaceholder: String,
    val txIncomePlaceholder: String,
    val categoryLabel: String,
    val attachReceipt: String,
    val changeReceipt: String,
    val optionalNoteLabel: String,
    val amountZeroError: String,
    val saveTransactionPrefix: (String) -> String,

    // Activities
    val activitiesTitle: String,
    val activitiesSubtitle: String,
    val quickAddLabel: String,
    val todayTargetTitle: String,
    val percentAchieved: (Int) -> String,
    val doneFraction: (Int, Int) -> String,
    val activeDurationLabel: String,
    val minutesUnit: (Int) -> String,
    val bestStreakLabel: String,
    val daysUnit: (Int) -> String,
    val streakShort: (Int) -> String,
    val emptyActivitiesFilterTitle: String,
    val emptyActivitiesTitle: String,
    val emptyActivitiesFilterSubtitle: String,
    val emptyActivitiesSubtitle: String,
    val addActivityAction: String,
    val editActivityTitle: String,
    val activityNameLabel: String,
    val activityNamePlaceholder: String,
    val targetDurationLabel: String,
    val minutesSuffix: String,
    val activityNotePlaceholder: String,
    val attachActivityPhoto: String,
    val changePhoto: String,
    val doneTodayLabel: String,
    val activityTitleRequiredError: String,
    val saveActivityButton: String,
    val deleteActivityConfirmTitle: String,
    val deleteActivityConfirmBody: (String) -> String,
    val markDoneToday: String,
    val markUndoneToday: String,

    // Backup & PDF Export Center
    val backupCenterTitle: String,
    val localOnlyTitle: String,
    val localOnlyDesc: String,
    val notesAndActivitiesStat: String,
    val financeTxStat: String,
    val photosStat: (String) -> String,
    val pendingExportStat: String,
    val pdfCardTitle: String,
    val pdfCardDesc: String,
    val exportPdfAction: String,
    val processing: String,
    val zipCardTitle: String,
    val zipLastBackup: (String) -> String,
    val zipEmptyDesc: String,
    val exportZipAction: String,
    val restoreZipAction: String,

    // Settings Sheet
    val settingsTitle: String,
    val settingsSubtitle: String,
    val languageSettingLabel: String,
    val fontFamilyLabel: String,
    val fontSizeLabel: String,
    val buttonSizeLabel: String,
    val themeModeLabel: String,
    val colorPaletteLabel: String,
    val backgroundLabel: String,
    val defaultBgNote: String,
    val pickBgPhoto: String,
    val changeBgPhoto: String,
    val readabilityOverlay: (Int) -> String,

    // Notifications & Reminders
    val openRemindersDesc: String,
    val remindersTitle: String,
    val remindersSubtitle: String,
    val notificationChannelName: String,
    val notificationChannelDesc: String,
    val notifPermissionMissingTitle: String,
    val notifPermissionMissingBody: String,
    val allowNotificationsAction: String,
    val notifPermissionActiveLabel: String,
    val testNotificationButton: String,
    val testNotificationSampleTitle: String,
    val testNotificationSampleBody: String,
    val testNotificationSentToast: String,
    val routineRemindersSection: String,
    val presetFinanceReminderTitle: String,
    val presetFinanceReminderBody: String,
    val presetActivitiesReminderTitle: String,
    val presetActivitiesReminderBody: String,
    val customRemindersSection: String,
    val addReminderButton: String,
    val noCustomRemindersYet: String,
    val reminderTitleLabel: String,
    val reminderTitlePlaceholder: String,
    val reminderMessageLabel: String,
    val reminderMessagePlaceholder: String,
    val quickLinkFromNotes: String,
    val reminderFrequencyLabel: String,
    val reminderOnceLabel: String,
    val reminderDailyLabel: String,
    val reminderDateLabel: String,
    val reminderTimeLabel: String,
    val reminderTargetTabLabel: String,
    val saveReminderAction: String,
    val reminderTitleRequiredError: String,
    val reminderPastTimeError: String,
    val reminderSavedMessage: (String) -> String,
    val reminderDeletedMessage: String,
    val reminderDailyPrefix: String,
    val reminderOncePrefix: String,
    val reminderStatusTriggered: String,
    val defaultReminderNotificationBody: String,
    val deleteReminderDesc: String,
    val pickTimeDialogTitle: String,
    val confirmAction: String,

    // Finance Chart
    val financeChartButton: String,
    val financeChartTitle: String,
    val financeChartSubtitle: String,
    val chartBarModeLabel: String,
    val chartDonutModeLabel: String,
    val chartTapHint: String,
    val chartEmptyMessage: String,
    val chartWeekPrefix: String,
    val chartCashflowShareTitle: String,
    val chartTopExpensesTitle: String,
    val chartTotalLabel: String,

    // Note Categories & Folders
    val folderActionChip: String,
    val manageFoldersTitle: String,
    val manageFoldersSubtitle: String,
    val createFolderFieldLabel: String,
    val createFolderFieldPlaceholder: String,
    val createFolderSubmit: String,
    val renameFolderTitle: String,
    val deleteFolderTitle: String,
    val deleteFolderConfirmBody: (String, Int) -> String,
    val folderNoteCount: (Int) -> String,
    val defaultFolderBadge: String,
    val folderEmptyNameError: String,
    val folderTooLongError: String,
    val folderDuplicateError: String,
    val folderProtectedError: String,
    val folderCreatedMessage: (String) -> String,
    val folderRenamedMessage: (String) -> String,
    val folderDeletedMessage: (String) -> String,
    val renameFolderDesc: String,
    val deleteFolderDesc: String,
    val addFolderDialogTitle: String
) {
    fun notesFilterLabel(filter: NotesFilter): String = when (thisLanguageKey) {
        AppLanguage.INDONESIAN -> filter.label
        AppLanguage.ENGLISH -> when (filter) {
            NotesFilter.ALL -> "All"
            NotesFilter.PINNED -> "Pinned"
            NotesFilter.WITH_IMAGES -> "With Photos"
        }
        AppLanguage.RUSSIAN -> when (filter) {
            NotesFilter.ALL -> "Все"
            NotesFilter.PINNED -> "Закреплённые"
            NotesFilter.WITH_IMAGES -> "С фото"
        }
        AppLanguage.MANDARIN -> when (filter) {
            NotesFilter.ALL -> "全部"
            NotesFilter.PINNED -> "已置顶"
            NotesFilter.WITH_IMAGES -> "含图片"
        }
    }

    fun financePeriodLabel(period: FinancePeriod): String = when (thisLanguageKey) {
        AppLanguage.INDONESIAN -> period.label
        AppLanguage.ENGLISH -> when (period) {
            FinancePeriod.THIS_MONTH -> "This Month"
            FinancePeriod.LAST_7_DAYS -> "7 Days"
            FinancePeriod.ALL_TIME -> "All"
        }
        AppLanguage.RUSSIAN -> when (period) {
            FinancePeriod.THIS_MONTH -> "Этот месяц"
            FinancePeriod.LAST_7_DAYS -> "7 дней"
            FinancePeriod.ALL_TIME -> "Все"
        }
        AppLanguage.MANDARIN -> when (period) {
            FinancePeriod.THIS_MONTH -> "本月"
            FinancePeriod.LAST_7_DAYS -> "近7天"
            FinancePeriod.ALL_TIME -> "全部"
        }
    }

    fun financeTypeFilterLabel(filter: FinanceTypeFilter): String = when (thisLanguageKey) {
        AppLanguage.INDONESIAN -> filter.label
        AppLanguage.ENGLISH -> when (filter) {
            FinanceTypeFilter.ALL -> "All"
            FinanceTypeFilter.INCOME -> "Income"
            FinanceTypeFilter.EXPENSE -> "Expense"
        }
        AppLanguage.RUSSIAN -> when (filter) {
            FinanceTypeFilter.ALL -> "Все"
            FinanceTypeFilter.INCOME -> "Доходы"
            FinanceTypeFilter.EXPENSE -> "Расходы"
        }
        AppLanguage.MANDARIN -> when (filter) {
            FinanceTypeFilter.ALL -> "全部"
            FinanceTypeFilter.INCOME -> "收入"
            FinanceTypeFilter.EXPENSE -> "支出"
        }
    }

    fun transactionTypeLabel(type: TransactionType): String = when (type) {
        TransactionType.INCOME -> incomeLabel
        TransactionType.EXPENSE -> expenseLabel
    }

    fun activityStatusFilterLabel(filter: ActivityStatusFilter): String = when (thisLanguageKey) {
        AppLanguage.INDONESIAN -> filter.label
        AppLanguage.ENGLISH -> when (filter) {
            ActivityStatusFilter.ALL -> "All"
            ActivityStatusFilter.PENDING -> "Pending"
            ActivityStatusFilter.COMPLETED -> "Done Today"
        }
        AppLanguage.RUSSIAN -> when (filter) {
            ActivityStatusFilter.ALL -> "Все"
            ActivityStatusFilter.PENDING -> "Не выполнено"
            ActivityStatusFilter.COMPLETED -> "Сделано"
        }
        AppLanguage.MANDARIN -> when (filter) {
            ActivityStatusFilter.ALL -> "全部"
            ActivityStatusFilter.PENDING -> "待完成"
            ActivityStatusFilter.COMPLETED -> "今日已完成"
        }
    }

    fun categoryLabel(raw: String): String = when (thisLanguageKey) {
        AppLanguage.INDONESIAN -> raw
        AppLanguage.ENGLISH -> when (raw) {
            "Umum" -> "General"
            "Kerja" -> "Work"
            "Pribadi" -> "Personal"
            "Ide" -> "Ideas"
            "Penting" -> "Important"
            "Belanja" -> "Shopping"
            "Perjalanan" -> "Travel"
            "Makan" -> "Food"
            "Transport" -> "Transport"
            "Tagihan" -> "Bills"
            "Kesehatan" -> "Health"
            "Hiburan" -> "Entertainment"
            "Gaji" -> "Salary"
            "Usaha" -> "Business"
            "Investasi" -> "Investment"
            "Bonus" -> "Bonus"
            "Hadiah" -> "Gift"
            "Olahraga" -> "Sports"
            "Belajar" -> "Study"
            "Ibadah" -> "Faith"
            "Produktivitas" -> "Productivity"
            "Hobi" -> "Hobby"
            "Lainnya" -> "Other"
            else -> raw
        }
        AppLanguage.RUSSIAN -> when (raw) {
            "Umum" -> "Общее"
            "Kerja" -> "Работа"
            "Pribadi" -> "Личное"
            "Ide" -> "Идеи"
            "Penting" -> "Важное"
            "Belanja" -> "Покупки"
            "Perjalanan" -> "Поездки"
            "Makan" -> "Еда"
            "Transport" -> "Транспорт"
            "Tagihan" -> "Счета"
            "Kesehatan" -> "Здоровье"
            "Hiburan" -> "Досуг"
            "Gaji" -> "Зарплата"
            "Usaha" -> "Бизнес"
            "Investasi" -> "Инвестиции"
            "Bonus" -> "Премия"
            "Hadiah" -> "Подарок"
            "Olahraga" -> "Спорт"
            "Belajar" -> "Учёба"
            "Ibadah" -> "Духовное"
            "Produktivitas" -> "Дела"
            "Hobi" -> "Хобби"
            "Lainnya" -> "Другое"
            else -> raw
        }
        AppLanguage.MANDARIN -> when (raw) {
            "Umum" -> "通用"
            "Kerja" -> "工作"
            "Pribadi" -> "个人"
            "Ide" -> "灵感"
            "Penting" -> "重要"
            "Belanja" -> "购物"
            "Perjalanan" -> "旅行"
            "Makan" -> "餐饮"
            "Transport" -> "交通"
            "Tagihan" -> "账单"
            "Kesehatan" -> "健康"
            "Hiburan" -> "娱乐"
            "Gaji" -> "工资"
            "Usaha" -> "生意"
            "Investasi" -> "投资"
            "Bonus" -> "奖金"
            "Hadiah" -> "礼金"
            "Olahraga" -> "运动"
            "Belajar" -> "学习"
            "Ibadah" -> "修身"
            "Produktivitas" -> "效率"
            "Hobi" -> "爱好"
            "Lainnya" -> "其他"
            else -> raw
        }
    }

    fun quickPresetTitle(raw: String): String = when (thisLanguageKey) {
        AppLanguage.INDONESIAN -> raw
        AppLanguage.ENGLISH -> when (raw) {
            "Lari / Jogging" -> "Run / Jogging"
            "Workout / Gym" -> "Workout / Gym"
            "Jalan Kaki" -> "Walking"
            "Membaca Buku" -> "Read Book"
            "Minum Air 2L" -> "Drink 2L Water"
            else -> raw
        }
        AppLanguage.RUSSIAN -> when (raw) {
            "Lari / Jogging" -> "Бег"
            "Workout / Gym" -> "Тренировка"
            "Jalan Kaki" -> "Прогулка"
            "Membaca Buku" -> "Чтение"
            "Minum Air 2L" -> "Вода 2л"
            else -> raw
        }
        AppLanguage.MANDARIN -> when (raw) {
            "Lari / Jogging" -> "跑步 / 慢跑"
            "Workout / Gym" -> "健身训练"
            "Jalan Kaki" -> "散步"
            "Membaca Buku" -> "阅读书籍"
            "Minum Air 2L" -> "喝水 2L"
            else -> raw
        }
    }

    fun fontChoiceLabel(choice: AppFontChoice): String = when (thisLanguageKey) {
        AppLanguage.INDONESIAN -> choice.label
        AppLanguage.ENGLISH -> when (choice) {
            AppFontChoice.PLUS_JAKARTA -> "Modern"
            AppFontChoice.PLAYFAIR -> "Classic Serif"
            AppFontChoice.DANCING_SCRIPT -> "Cursive"
            AppFontChoice.ROBOTO_MONO -> "Mono"
            AppFontChoice.SYSTEM -> "System"
        }
        AppLanguage.RUSSIAN -> when (choice) {
            AppFontChoice.PLUS_JAKARTA -> "Модерн"
            AppFontChoice.PLAYFAIR -> "Классика"
            AppFontChoice.DANCING_SCRIPT -> "Курсив"
            AppFontChoice.ROBOTO_MONO -> "Моно"
            AppFontChoice.SYSTEM -> "Системный"
        }
        AppLanguage.MANDARIN -> when (choice) {
            AppFontChoice.PLUS_JAKARTA -> "现代"
            AppFontChoice.PLAYFAIR -> "经典衬线"
            AppFontChoice.DANCING_SCRIPT -> "手写花体"
            AppFontChoice.ROBOTO_MONO -> "等宽"
            AppFontChoice.SYSTEM -> "系统默认"
        }
    }

    fun scaleOrSizeLabel(key: String): String = when (thisLanguageKey) {
        AppLanguage.INDONESIAN -> when (key) {
            "SMALL", "COMPACT" -> "Kecil"
            "NORMAL", "MEDIUM" -> "Sedang"
            else -> "Besar"
        }
        AppLanguage.ENGLISH -> when (key) {
            "SMALL", "COMPACT" -> "Small"
            "NORMAL", "MEDIUM" -> "Medium"
            else -> "Large"
        }
        AppLanguage.RUSSIAN -> when (key) {
            "SMALL", "COMPACT" -> "Малый"
            "NORMAL", "MEDIUM" -> "Средний"
            else -> "Большой"
        }
        AppLanguage.MANDARIN -> when (key) {
            "SMALL", "COMPACT" -> "小"
            "NORMAL", "MEDIUM" -> "中"
            else -> "大"
        }
    }

    fun fontScaleLabel(scale: AppFontScale): String = scaleOrSizeLabel(scale.key)
    fun buttonSizeLabel(size: AppButtonSize): String = scaleOrSizeLabel(size.key)

    fun themeModeLabel(mode: AppThemeMode): String = when (thisLanguageKey) {
        AppLanguage.INDONESIAN -> mode.label
        AppLanguage.ENGLISH -> when (mode) {
            AppThemeMode.SYSTEM -> "System"
            AppThemeMode.LIGHT -> "Light"
            AppThemeMode.DARK -> "Dark"
        }
        AppLanguage.RUSSIAN -> when (mode) {
            AppThemeMode.SYSTEM -> "Система"
            AppThemeMode.LIGHT -> "Светлая"
            AppThemeMode.DARK -> "Тёмная"
        }
        AppLanguage.MANDARIN -> when (mode) {
            AppThemeMode.SYSTEM -> "跟随系统"
            AppThemeMode.LIGHT -> "浅色"
            AppThemeMode.DARK -> "深色"
        }
    }

    fun themePresetLabel(preset: AppThemePreset): String = when (thisLanguageKey) {
        AppLanguage.INDONESIAN -> preset.label
        AppLanguage.ENGLISH -> when (preset) {
            AppThemePreset.EMERALD -> "Emerald"
            AppThemePreset.INDIGO -> "Ocean"
            AppThemePreset.TERRACOTTA -> "Terracotta"
            AppThemePreset.BOTANICAL -> "Forest"
            AppThemePreset.PLUM -> "Plum"
        }
        AppLanguage.RUSSIAN -> when (preset) {
            AppThemePreset.EMERALD -> "Изумруд"
            AppThemePreset.INDIGO -> "Океан"
            AppThemePreset.TERRACOTTA -> "Терракота"
            AppThemePreset.BOTANICAL -> "Лес"
            AppThemePreset.PLUM -> "Слива"
        }
        AppLanguage.MANDARIN -> when (preset) {
            AppThemePreset.EMERALD -> "翠绿"
            AppThemePreset.INDIGO -> "深海"
            AppThemePreset.TERRACOTTA -> "陶土"
            AppThemePreset.BOTANICAL -> "森林"
            AppThemePreset.PLUM -> "紫梅"
        }
    }

    fun bgModeLabel(mode: BackgroundMode): String = when (thisLanguageKey) {
        AppLanguage.INDONESIAN -> mode.label
        AppLanguage.ENGLISH -> when (mode) {
            BackgroundMode.DEFAULT -> "Default"
            BackgroundMode.GRADIENT -> "Gradient"
            BackgroundMode.SOLID -> "Solid"
            BackgroundMode.IMAGE -> "Gallery Photo"
        }
        AppLanguage.RUSSIAN -> when (mode) {
            BackgroundMode.DEFAULT -> "По умолч."
            BackgroundMode.GRADIENT -> "Градиент"
            BackgroundMode.SOLID -> "Цвет"
            BackgroundMode.IMAGE -> "Фото"
        }
        AppLanguage.MANDARIN -> when (mode) {
            BackgroundMode.DEFAULT -> "默认"
            BackgroundMode.GRADIENT -> "渐变"
            BackgroundMode.SOLID -> "纯色"
            BackgroundMode.IMAGE -> "相册图片"
        }
    }

    fun bgGradientLabel(grad: BackgroundGradientPreset): String = when (thisLanguageKey) {
        AppLanguage.INDONESIAN -> grad.label
        AppLanguage.ENGLISH -> when (grad) {
            BackgroundGradientPreset.WARM_PAPER -> "Warm Cream"
            BackgroundGradientPreset.EMERALD_MIST -> "Emerald Mist"
            BackgroundGradientPreset.SUNSET_DUSK -> "Soft Dusk"
            BackgroundGradientPreset.OCEAN_BREEZE -> "Ocean Breeze"
            BackgroundGradientPreset.NIGHT_AURORA -> "Night Aurora"
        }
        AppLanguage.RUSSIAN -> when (grad) {
            BackgroundGradientPreset.WARM_PAPER -> "Крем"
            BackgroundGradientPreset.EMERALD_MIST -> "Изумруд"
            BackgroundGradientPreset.SUNSET_DUSK -> "Закат"
            BackgroundGradientPreset.OCEAN_BREEZE -> "Бриз"
            BackgroundGradientPreset.NIGHT_AURORA -> "Ночь"
        }
        AppLanguage.MANDARIN -> when (grad) {
            BackgroundGradientPreset.WARM_PAPER -> "暖米"
            BackgroundGradientPreset.EMERALD_MIST -> "翡翠雾"
            BackgroundGradientPreset.SUNSET_DUSK -> "晚霞"
            BackgroundGradientPreset.OCEAN_BREEZE -> "海风"
            BackgroundGradientPreset.NIGHT_AURORA -> "极光夜"
        }
    }

    fun bgSolidLabel(solid: BackgroundSolidPreset): String = when (thisLanguageKey) {
        AppLanguage.INDONESIAN -> solid.label
        AppLanguage.ENGLISH -> when (solid) {
            BackgroundSolidPreset.IVORY -> "Ivory"
            BackgroundSolidPreset.WARM_SAND -> "Sand"
            BackgroundSolidPreset.MINT_SOFT -> "Mint"
            BackgroundSolidPreset.SKY_SOFT -> "Sky"
            BackgroundSolidPreset.BLUSH_SOFT -> "Rose"
            BackgroundSolidPreset.LAVENDER_SOFT -> "Lavender"
            BackgroundSolidPreset.SLATE_DARK -> "Charcoal"
            BackgroundSolidPreset.MIDNIGHT_BLUE -> "Midnight"
        }
        AppLanguage.RUSSIAN -> when (solid) {
            BackgroundSolidPreset.IVORY -> "Слоновая"
            BackgroundSolidPreset.WARM_SAND -> "Песок"
            BackgroundSolidPreset.MINT_SOFT -> "Мята"
            BackgroundSolidPreset.SKY_SOFT -> "Небо"
            BackgroundSolidPreset.BLUSH_SOFT -> "Роза"
            BackgroundSolidPreset.LAVENDER_SOFT -> "Лаванда"
            BackgroundSolidPreset.SLATE_DARK -> "Уголь"
            BackgroundSolidPreset.MIDNIGHT_BLUE -> "Полночь"
        }
        AppLanguage.MANDARIN -> when (solid) {
            BackgroundSolidPreset.IVORY -> "象牙白"
            BackgroundSolidPreset.WARM_SAND -> "暖沙"
            BackgroundSolidPreset.MINT_SOFT -> "薄荷"
            BackgroundSolidPreset.SKY_SOFT -> "晴空"
            BackgroundSolidPreset.BLUSH_SOFT -> "浅粉"
            BackgroundSolidPreset.LAVENDER_SOFT -> "薰衣草"
            BackgroundSolidPreset.SLATE_DARK -> "炭黑"
            BackgroundSolidPreset.MIDNIGHT_BLUE -> "午夜蓝"
        }
    }

    private val thisLanguageKey: AppLanguage
        get() = when (tabNotes) {
            "Catatan" -> AppLanguage.INDONESIAN
            "Notes" -> AppLanguage.ENGLISH
            "Заметки" -> AppLanguage.RUSSIAN
            else -> AppLanguage.MANDARIN
        }
}

fun indonesianStrings() = AppStrings(
    tabNotes = "Catatan",
    tabFinance = "Keuangan",
    tabActivities = "Aktivitas",
    justNow = "Baru saja",
    minutesAgo = { "$it mnt lalu" },
    hoursAgo = { "$it jam lalu" },
    yesterday = "Kemarin",
    cancel = "Batal",
    delete = "Hapus",
    saveChanges = "Simpan Perubahan",
    select = "Pilih",
    close = "Tutup",
    reset = "Reset",
    showAll = "Tampilkan Semua",
    clearSearch = "Hapus pencarian",
    back = "Kembali",
    pdfChip = "PDF",
    exportPdfShort = "Ekspor PDF",
    openSettingsDesc = "Pengaturan bahasa, huruf, tema, dan ukuran tombol",
    notesStatsSummary = { notes, photos -> "$notes catatan • $photos foto" },
    localNoticeCompact = "Lokal aktif • Ekspor ke PDF",
    searchNotesHint = "Cari judul, isi, atau tag…",
    toggleToList = "Ubah ke tampilan daftar",
    toggleToGrid = "Ubah ke tampilan grid",
    emptyNotesTitle = "Belum Ada Catatan",
    emptyNotesSubtitle = "Mulai tulis ide, tugas, atau sisipkan foto penting Anda. Semua tersimpan aman di perangkat dan siap diekspor.",
    emptySearchTitle = "Tidak Ada Hasil",
    emptySearchSubtitle = "Coba kata kunci atau filter kategori lain.",
    createNoteButton = "Buat Catatan",
    newNoteAction = "Catatan Baru",
    deleteNoteConfirmTitle = "Hapus Catatan Ini?",
    deleteNoteConfirmBody = "Catatan beserta seluruh foto terlampir di dalamnya akan dihapus secara permanen dari perangkat.",
    photoNoteFallback = "Catatan bergambar",
    statusBackedUp = "Dicadangkan",
    statusLocal = "Lokal",
    pinNote = "Sematkan",
    unpinNote = "Lepas Sematan",
    deleteNoteAction = "Hapus Catatan",
    autoSaved = "Tersimpan otomatis",
    saving = "Menyimpan…",
    editorTitleHint = "Judul catatan…",
    editorBodyHint = "Tulis catatan di sini…",
    gallery = "Galeri",
    camera = "Kamera",
    photoCount = { "$it foto" },
    shareNote = "Bagikan",
    exportNotePdfDesc = "Ekspor catatan ke PDF",
    removeImageDesc = "Hapus gambar",
    fullscreenPhotoDesc = { "Tampilan penuh foto $it" },
    deleteImageDesc = "Hapus gambar ini",
    financeTitle = "Keuangan",
    cashFlowPrefix = "Arus kas",
    netBalanceTitle = "Saldo Bersih",
    txCountLabel = { "$it transaksi" },
    incomeLabel = "Pemasukan",
    expenseLabel = "Pengeluaran",
    expensePerCategory = "Pengeluaran per Kategori",
    searchFinanceHint = "Cari transaksi atau kategori…",
    financeEmptyTitle = "Belum Ada Catatan Keuangan",
    financeEmptySubtitle = "Catat pemasukan dan pengeluaran harian beserta foto struk agar arus kas tetap terkontrol.",
    addTransactionAction = "Catat Transaksi",
    editTransactionTitle = "Ubah Transaksi",
    deleteTxConfirmTitle = "Hapus Transaksi Ini?",
    deleteTxConfirmBody = { "Catatan transaksi \"$it\" akan dihapus dari riwayat keuangan." },
    amountLabel = "Nominal (Rp)",
    amountReadAs = { "Terbaca: $it" },
    txTitleLabel = "Judul / Keterangan",
    txExpensePlaceholder = "Contoh: Belanja mingguan, Kopi pagi",
    txIncomePlaceholder = "Contoh: Gaji bulanan, Pembayaran proyek",
    categoryLabel = "Kategori",
    attachReceipt = "Foto Struk",
    changeReceipt = "Ganti Struk",
    optionalNoteLabel = "Catatan tambahan (opsional)",
    amountZeroError = "Masukkan nominal lebih dari Rp 0",
    saveTransactionPrefix = { "Simpan $it" },
    activitiesTitle = "Aktivitas Harian",
    activitiesSubtitle = "Olahraga, kebiasaan & rutinitas",
    quickAddLabel = "Tambah Cepat",
    todayTargetTitle = "Target Hari Ini",
    percentAchieved = { "$it% tercapai" },
    doneFraction = { done, total -> "$done / $total Selesai" },
    activeDurationLabel = "Durasi Aktif",
    minutesUnit = { "$it menit" },
    bestStreakLabel = "Streak Terbaik",
    daysUnit = { "$it hari" },
    streakShort = { "${it}h" },
    emptyActivitiesFilterTitle = "Tidak Ada Aktivitas Cocok",
    emptyActivitiesTitle = "Belum Ada Aktivitas Harian",
    emptyActivitiesFilterSubtitle = "Coba pilih filter status atau kategori aktivitas lain.",
    emptyActivitiesSubtitle = "Mulai catat olahraga, membaca, minum air, atau rutinitas harian Anda untuk membangun streak!",
    addActivityAction = "Tambah Aktivitas",
    editActivityTitle = "Ubah Aktivitas",
    activityNameLabel = "Nama Aktivitas",
    activityNamePlaceholder = "Contoh: Lari pagi, Gym, Membaca buku",
    targetDurationLabel = "Target Durasi (menit, 0 jika tanpa durasi)",
    minutesSuffix = "menit",
    activityNotePlaceholder = "Contoh: 5 km keliling taman, 20 halaman",
    attachActivityPhoto = "Foto Aktivitas",
    changePhoto = "Ganti Foto",
    doneTodayLabel = "Selesai hari ini",
    activityTitleRequiredError = "Masukkan nama aktivitas terlebih dahulu",
    saveActivityButton = "Simpan Aktivitas",
    deleteActivityConfirmTitle = "Hapus Aktivitas Ini?",
    deleteActivityConfirmBody = { "Aktivitas \"$it\" beserta riwayat streak-nya akan dihapus." },
    markDoneToday = "Tandai selesai hari ini",
    markUndoneToday = "Tandai belum selesai hari ini",
    backupCenterTitle = "Ekspor PDF & Cadangan",
    localOnlyTitle = "Penyimpanan Lokal Aktif",
    localOnlyDesc = "Sinkronisasi cloud Firebase belum diaktifkan. Seluruh data tersimpan lokal dan dapat diekspor menjadi dokumen PDF atau cadangan ZIP kapan saja.",
    notesAndActivitiesStat = "Catatan & Aktivitas",
    financeTxStat = "Transaksi Keuangan",
    photosStat = { "Foto ($it)" },
    pendingExportStat = "Belum Diekspor",
    pdfCardTitle = "Ekspor Dokumen PDF (.pdf)",
    pdfCardDesc = "Ekspor seluruh catatan beserta foto, laporan keuangan + struk, dan aktivitas harian ke satu dokumen PDF siap baca & cetak.",
    exportPdfAction = "Ekspor ke PDF (.pdf)",
    processing = "Memproses…",
    zipCardTitle = "Cadangan & Pulihkan Data (.zip)",
    zipLastBackup = { "Cadangan ZIP terakhir: $it" },
    zipEmptyDesc = "Simpan arsip ZIP untuk memindahkan atau memulihkan seluruh data antar perangkat.",
    exportZipAction = "Cadangan ZIP",
    restoreZipAction = "Pulihkan ZIP",
    settingsTitle = "Pengaturan",
    settingsSubtitle = "Bahasa, huruf, tema, ukuran teks & tombol",
    languageSettingLabel = "Bahasa",
    fontFamilyLabel = "Huruf / Font",
    fontSizeLabel = "Ukuran Font",
    buttonSizeLabel = "Ukuran Tombol (Button)",
    themeModeLabel = "Mode Tema",
    colorPaletteLabel = "Palet Warna Tema",
    backgroundLabel = "Latar Belakang",
    defaultBgNote = "Menggunakan latar bawaan sesuai mode Terang / Gelap.",
    pickBgPhoto = "Pilih Foto dari Galeri",
    changeBgPhoto = "Ganti Foto Latar",
    readabilityOverlay = { "Lapisan Keterbacaan ($it%)" },
    openRemindersDesc = "Notifikasi dan pengingat jadwal",
    remindersTitle = "Notifikasi & Pengingat",
    remindersSubtitle = "Jadwalkan pengingat catatan, keuangan & rutinitas",
    notificationChannelName = "Pengingat Note All",
    notificationChannelDesc = "Notifikasi pengingat catatan, keuangan, dan aktivitas harian",
    notifPermissionMissingTitle = "Izin Notifikasi Belum Aktif",
    notifPermissionMissingBody = "Izinkan notifikasi agar pengingat dapat berbunyi dan muncul tepat waktu.",
    allowNotificationsAction = "Izinkan Notifikasi",
    notifPermissionActiveLabel = "Notifikasi aktif",
    testNotificationButton = "Tes Notif",
    testNotificationSampleTitle = "Pengingat Note All Aktif",
    testNotificationSampleBody = "Notifikasi berhasil! Pengingat Anda siap berbunyi tepat waktu.",
    testNotificationSentToast = "Notifikasi percobaan dikirim",
    routineRemindersSection = "Pengingat Rutin Harian",
    presetFinanceReminderTitle = "Catat Keuangan Hari Ini",
    presetFinanceReminderBody = "Jangan lupa catat pemasukan dan pengeluaran Anda hari ini di Note All.",
    presetActivitiesReminderTitle = "Cek Aktivitas Harian",
    presetActivitiesReminderBody = "Waktunya olahraga atau selesaikan kebiasaan harian Anda untuk menjaga streak!",
    customRemindersSection = "Pengingat Jadwal & Catatan",
    addReminderButton = "Tambah",
    noCustomRemindersYet = "Belum ada pengingat khusus. Tekan + Tambah untuk menjadwalkan pengingat catatan atau tugas.",
    reminderTitleLabel = "Judul Pengingat",
    reminderTitlePlaceholder = "Contoh: Bayar listrik, Rapat tim, Minum vitamin",
    reminderMessageLabel = "Pesan Notifikasi (opsional)",
    reminderMessagePlaceholder = "Detail pengingat yang tampil di notifikasi…",
    quickLinkFromNotes = "Ambil dari Catatan",
    reminderFrequencyLabel = "Frekuensi",
    reminderOnceLabel = "Sekali",
    reminderDailyLabel = "Tiap Hari",
    reminderDateLabel = "Tanggal",
    reminderTimeLabel = "Jam",
    reminderTargetTabLabel = "Buka Bagian",
    saveReminderAction = "Simpan Pengingat",
    reminderTitleRequiredError = "Masukkan judul pengingat terlebih dahulu",
    reminderPastTimeError = "Pilih tanggal dan jam di masa depan",
    reminderSavedMessage = { "Pengingat dijadwalkan pukul $it" },
    reminderDeletedMessage = "Pengingat dihapus",
    reminderDailyPrefix = "Harian",
    reminderOncePrefix = "Sekali",
    reminderStatusTriggered = "Sudah berbunyi",
    defaultReminderNotificationBody = "Ketuk untuk membuka pengingat Anda di Note All.",
    deleteReminderDesc = "Hapus pengingat",
    pickTimeDialogTitle = "Pilih Jam Pengingat",
    confirmAction = "Pilih",
    financeChartButton = "Grafik Pemasukan & Pengeluaran",
    financeChartTitle = "Grafik Keuangan",
    financeChartSubtitle = "Perbandingan pemasukan & pengeluaran",
    chartBarModeLabel = "Grafik Batang",
    chartDonutModeLabel = "Proporsi",
    chartTapHint = "Ketuk batang grafik untuk melihat detail nominal",
    chartEmptyMessage = "Belum ada transaksi pada periode ini untuk ditampilkan pada grafik.",
    chartWeekPrefix = "M",
    chartCashflowShareTitle = "Perbandingan Arus Kas",
    chartTopExpensesTitle = "Distribusi Pengeluaran",
    chartTotalLabel = "Total",
    folderActionChip = "Folder",
    manageFoldersTitle = "Kategori & Folder Catatan",
    manageFoldersSubtitle = "Buat, ubah nama, atau hapus folder untuk mengelompokkan catatan",
    createFolderFieldLabel = "Nama Folder / Kategori Baru",
    createFolderFieldPlaceholder = "Contoh: Kuliah, Resep, Proyek",
    createFolderSubmit = "Buat",
    renameFolderTitle = "Ubah Nama Folder",
    deleteFolderTitle = "Hapus Folder Ini?",
    deleteFolderConfirmBody = { name, count ->
        if (count > 0) {
            "Folder \"$name\" akan dihapus. $count catatan di dalamnya akan dipindahkan dengan aman ke Umum."
        } else {
            "Folder \"$name\" akan dihapus dari daftar kategori."
        }
    },
    folderNoteCount = { "$it catatan" },
    defaultFolderBadge = "Utama",
    folderEmptyNameError = "Masukkan nama folder terlebih dahulu",
    folderTooLongError = "Maksimal 32 karakter",
    folderDuplicateError = "Folder tersebut sudah ada",
    folderProtectedError = "Folder utama tidak dapat diubah",
    folderCreatedMessage = { "Folder \"$it\" dibuat" },
    folderRenamedMessage = { "Folder diubah menjadi \"$it\"" },
    folderDeletedMessage = { "Folder \"$it\" dihapus" },
    renameFolderDesc = "Ubah nama folder",
    deleteFolderDesc = "Hapus folder",
    addFolderDialogTitle = "Buat Folder Baru"
)

fun englishStrings() = AppStrings(
    tabNotes = "Notes",
    tabFinance = "Finance",
    tabActivities = "Activities",
    justNow = "Just now",
    minutesAgo = { "${it}m ago" },
    hoursAgo = { "${it}h ago" },
    yesterday = "Yesterday",
    cancel = "Cancel",
    delete = "Delete",
    saveChanges = "Save Changes",
    select = "Select",
    close = "Close",
    reset = "Reset",
    showAll = "Show All",
    clearSearch = "Clear search",
    back = "Back",
    pdfChip = "PDF",
    exportPdfShort = "Export PDF",
    openSettingsDesc = "Language, font, theme, and button size settings",
    notesStatsSummary = { notes, photos -> "$notes notes • $photos photos" },
    localNoticeCompact = "Local storage • Export to PDF",
    searchNotesHint = "Search title, content, or tag…",
    toggleToList = "Switch to list view",
    toggleToGrid = "Switch to grid view",
    emptyNotesTitle = "No Notes Yet",
    emptyNotesSubtitle = "Start writing ideas, tasks, or attach important photos. Everything stays safely on your device and ready to export.",
    emptySearchTitle = "No Results",
    emptySearchSubtitle = "Try a different keyword or category filter.",
    createNoteButton = "Create Note",
    newNoteAction = "New Note",
    deleteNoteConfirmTitle = "Delete This Note?",
    deleteNoteConfirmBody = "This note and all its attached photos will be permanently deleted from your device.",
    photoNoteFallback = "Photo note",
    statusBackedUp = "Backed up",
    statusLocal = "Local",
    pinNote = "Pin",
    unpinNote = "Unpin",
    deleteNoteAction = "Delete Note",
    autoSaved = "Auto-saved",
    saving = "Saving…",
    editorTitleHint = "Note title…",
    editorBodyHint = "Write your note here…",
    gallery = "Gallery",
    camera = "Camera",
    photoCount = { "$it photos" },
    shareNote = "Share",
    exportNotePdfDesc = "Export note to PDF",
    removeImageDesc = "Remove photo",
    fullscreenPhotoDesc = { "Fullscreen photo $it" },
    deleteImageDesc = "Delete this photo",
    financeTitle = "Finance",
    cashFlowPrefix = "Cash flow",
    netBalanceTitle = "Net Balance",
    txCountLabel = { "$it transactions" },
    incomeLabel = "Income",
    expenseLabel = "Expense",
    expensePerCategory = "Expenses by Category",
    searchFinanceHint = "Search transactions or category…",
    financeEmptyTitle = "No Finance Records",
    financeEmptySubtitle = "Track daily income and expenses along with receipt photos to keep your cash flow under control.",
    addTransactionAction = "Add Transaction",
    editTransactionTitle = "Edit Transaction",
    deleteTxConfirmTitle = "Delete Transaction?",
    deleteTxConfirmBody = { "Transaction \"$it\" will be removed from your finance history." },
    amountLabel = "Amount (Rp)",
    amountReadAs = { "Parsed: $it" },
    txTitleLabel = "Title / Description",
    txExpensePlaceholder = "e.g. Groceries, Morning coffee",
    txIncomePlaceholder = "e.g. Monthly salary, Project payment",
    categoryLabel = "Category",
    attachReceipt = "Receipt Photo",
    changeReceipt = "Change Receipt",
    optionalNoteLabel = "Extra note (optional)",
    amountZeroError = "Enter an amount greater than Rp 0",
    saveTransactionPrefix = { "Save $it" },
    activitiesTitle = "Daily Activities",
    activitiesSubtitle = "Exercise, habits & routines",
    quickAddLabel = "Quick Add",
    todayTargetTitle = "Today's Goal",
    percentAchieved = { "$it% achieved" },
    doneFraction = { done, total -> "$done / $total Done" },
    activeDurationLabel = "Active Time",
    minutesUnit = { "$it min" },
    bestStreakLabel = "Best Streak",
    daysUnit = { "$it days" },
    streakShort = { "${it}d" },
    emptyActivitiesFilterTitle = "No Matching Activities",
    emptyActivitiesTitle = "No Daily Activities Yet",
    emptyActivitiesFilterSubtitle = "Try selecting another status or category filter.",
    emptyActivitiesSubtitle = "Start logging workouts, reading, hydration, or daily habits to build your streak!",
    addActivityAction = "Add Activity",
    editActivityTitle = "Edit Activity",
    activityNameLabel = "Activity Name",
    activityNamePlaceholder = "e.g. Morning run, Gym, Reading",
    targetDurationLabel = "Target Duration (minutes, 0 if none)",
    minutesSuffix = "min",
    activityNotePlaceholder = "e.g. 5 km around the park, 20 pages",
    attachActivityPhoto = "Activity Photo",
    changePhoto = "Change Photo",
    doneTodayLabel = "Done today",
    activityTitleRequiredError = "Please enter an activity name",
    saveActivityButton = "Save Activity",
    deleteActivityConfirmTitle = "Delete This Activity?",
    deleteActivityConfirmBody = { "Activity \"$it\" and its streak history will be deleted." },
    markDoneToday = "Mark done today",
    markUndoneToday = "Mark not done today",
    backupCenterTitle = "PDF Export & Backup",
    localOnlyTitle = "Local Storage Active",
    localOnlyDesc = "Firebase cloud sync is disabled. All data is stored locally and can be exported to PDF or ZIP backup anytime.",
    notesAndActivitiesStat = "Notes & Activities",
    financeTxStat = "Transactions",
    photosStat = { "Photos ($it)" },
    pendingExportStat = "Pending Backup",
    pdfCardTitle = "Export PDF Document (.pdf)",
    pdfCardDesc = "Export all notes with photos, finance ledger + receipts, and daily activities into one printable PDF.",
    exportPdfAction = "Export to PDF (.pdf)",
    processing = "Processing…",
    zipCardTitle = "ZIP Backup & Restore (.zip)",
    zipLastBackup = { "Last ZIP backup: $it" },
    zipEmptyDesc = "Save a ZIP archive to transfer or restore all data across devices.",
    exportZipAction = "Export ZIP",
    restoreZipAction = "Restore ZIP",
    settingsTitle = "Settings",
    settingsSubtitle = "Language, font, theme, text & button size",
    languageSettingLabel = "Language",
    fontFamilyLabel = "Font Family",
    fontSizeLabel = "Font Size",
    buttonSizeLabel = "Button Size",
    themeModeLabel = "Theme Mode",
    colorPaletteLabel = "Color Palette",
    backgroundLabel = "Background",
    defaultBgNote = "Uses default background matching Light / Dark mode.",
    pickBgPhoto = "Pick Photo from Gallery",
    changeBgPhoto = "Change Background Photo",
    readabilityOverlay = { "Readability Overlay ($it%)" },
    openRemindersDesc = "Notifications and scheduled reminders",
    remindersTitle = "Notifications & Reminders",
    remindersSubtitle = "Schedule reminders for notes, finance & habits",
    notificationChannelName = "Note All Reminders",
    notificationChannelDesc = "Reminder alerts for notes, finance, and daily activities",
    notifPermissionMissingTitle = "Notification Permission Needed",
    notifPermissionMissingBody = "Allow notifications so scheduled reminders can alert you on time.",
    allowNotificationsAction = "Allow Notifications",
    notifPermissionActiveLabel = "Notifications enabled",
    testNotificationButton = "Test Notif",
    testNotificationSampleTitle = "Note All Notifications Ready",
    testNotificationSampleBody = "Notification test succeeded! Your reminders are ready to alert you.",
    testNotificationSentToast = "Test notification sent",
    routineRemindersSection = "Daily Routine Reminders",
    presetFinanceReminderTitle = "Log Today's Finance",
    presetFinanceReminderBody = "Take a moment to record today's income and expenses in Note All.",
    presetActivitiesReminderTitle = "Daily Activities Check-In",
    presetActivitiesReminderBody = "Time to complete your daily habits and keep your streak going!",
    customRemindersSection = "Scheduled & Note Reminders",
    addReminderButton = "Add",
    noCustomRemindersYet = "No custom reminders yet. Tap + Add to schedule a note or task reminder.",
    reminderTitleLabel = "Reminder Title",
    reminderTitlePlaceholder = "e.g. Pay bills, Team meeting, Take vitamins",
    reminderMessageLabel = "Notification Message (optional)",
    reminderMessagePlaceholder = "Extra details shown in the notification…",
    quickLinkFromNotes = "Link from Notes",
    reminderFrequencyLabel = "Repeat",
    reminderOnceLabel = "Once",
    reminderDailyLabel = "Daily",
    reminderDateLabel = "Date",
    reminderTimeLabel = "Time",
    reminderTargetTabLabel = "Open Section",
    saveReminderAction = "Save Reminder",
    reminderTitleRequiredError = "Please enter a reminder title",
    reminderPastTimeError = "Choose a future date and time",
    reminderSavedMessage = { "Reminder scheduled for $it" },
    reminderDeletedMessage = "Reminder deleted",
    reminderDailyPrefix = "Daily",
    reminderOncePrefix = "Once",
    reminderStatusTriggered = "Completed",
    defaultReminderNotificationBody = "Tap to open your reminder in Note All.",
    deleteReminderDesc = "Delete reminder",
    pickTimeDialogTitle = "Pick Reminder Time",
    confirmAction = "OK",
    financeChartButton = "Income & Expense Charts",
    financeChartTitle = "Finance Charts",
    financeChartSubtitle = "Income vs expense visual breakdown",
    chartBarModeLabel = "Bar Chart",
    chartDonutModeLabel = "Proportion",
    chartTapHint = "Tap any bar to inspect exact amounts",
    chartEmptyMessage = "No transactions in this period yet to plot on the chart.",
    chartWeekPrefix = "W",
    chartCashflowShareTitle = "Cash Flow Ratio",
    chartTopExpensesTitle = "Expense Breakdown",
    chartTotalLabel = "Total",
    folderActionChip = "Folders",
    manageFoldersTitle = "Note Categories & Folders",
    manageFoldersSubtitle = "Create, rename, or remove folders to keep notes organized",
    createFolderFieldLabel = "New Folder / Category Name",
    createFolderFieldPlaceholder = "e.g. Study, Recipes, Projects",
    createFolderSubmit = "Create",
    renameFolderTitle = "Rename Folder",
    deleteFolderTitle = "Delete This Folder?",
    deleteFolderConfirmBody = { name, count ->
        if (count > 0) {
            "Folder \"$name\" will be removed. Its $count notes will be safely moved to General."
        } else {
            "Folder \"$name\" will be removed from your category list."
        }
    },
    folderNoteCount = { "$it notes" },
    defaultFolderBadge = "Default",
    folderEmptyNameError = "Please enter a folder name",
    folderTooLongError = "Maximum 32 characters",
    folderDuplicateError = "That folder already exists",
    folderProtectedError = "Default folder cannot be modified",
    folderCreatedMessage = { "Folder \"$it\" created" },
    folderRenamedMessage = { "Renamed folder to \"$it\"" },
    folderDeletedMessage = { "Folder \"$it\" deleted" },
    renameFolderDesc = "Rename folder",
    deleteFolderDesc = "Delete folder",
    addFolderDialogTitle = "Create New Folder"
)

fun russianStrings() = AppStrings(
    tabNotes = "Заметки",
    tabFinance = "Финансы",
    tabActivities = "Активность",
    justNow = "Только что",
    minutesAgo = { "$it мин назад" },
    hoursAgo = { "$it ч назад" },
    yesterday = "Вчера",
    cancel = "Отмена",
    delete = "Удалить",
    saveChanges = "Сохранить",
    select = "Выбрать",
    close = "Закрыть",
    reset = "Сброс",
    showAll = "Показать все",
    clearSearch = "Очистить поиск",
    back = "Назад",
    pdfChip = "PDF",
    exportPdfShort = "Экспорт PDF",
    openSettingsDesc = "Настройки языка, шрифта, темы и кнопок",
    notesStatsSummary = { notes, photos -> "$notes зам. • $photos фото" },
    localNoticeCompact = "Локально • Экспорт в PDF",
    searchNotesHint = "Поиск по заголовку, тексту, тегу…",
    toggleToList = "Вид списка",
    toggleToGrid = "Вид сетки",
    emptyNotesTitle = "Пока нет заметок",
    emptyNotesSubtitle = "Записывайте идеи, задачи и прикрепляйте фото. Всё хранится на устройстве и готово к экспорту.",
    emptySearchTitle = "Ничего не найдено",
    emptySearchSubtitle = "Попробуйте другой запрос или фильтр.",
    createNoteButton = "Создать заметку",
    newNoteAction = "Новая заметка",
    deleteNoteConfirmTitle = "Удалить заметку?",
    deleteNoteConfirmBody = "Заметка и все прикреплённые фото будут удалены с устройства безвозвратно.",
    photoNoteFallback = "Заметка с фото",
    statusBackedUp = "В архиве",
    statusLocal = "Локально",
    pinNote = "Закрепить",
    unpinNote = "Открепить",
    deleteNoteAction = "Удалить заметку",
    autoSaved = "Сохранено",
    saving = "Сохранение…",
    editorTitleHint = "Заголовок…",
    editorBodyHint = "Напишите заметку здесь…",
    gallery = "Галерея",
    camera = "Камера",
    photoCount = { "$it фото" },
    shareNote = "Поделиться",
    exportNotePdfDesc = "Экспорт заметки в PDF",
    removeImageDesc = "Удалить фото",
    fullscreenPhotoDesc = { "Фото $it во весь экран" },
    deleteImageDesc = "Удалить это фото",
    financeTitle = "Финансы",
    cashFlowPrefix = "Поток",
    netBalanceTitle = "Чистый баланс",
    txCountLabel = { "$it опер." },
    incomeLabel = "Доход",
    expenseLabel = "Расход",
    expensePerCategory = "Расходы по категориям",
    searchFinanceHint = "Поиск операций или категорий…",
    financeEmptyTitle = "Нет финансовых записей",
    financeEmptySubtitle = "Учитывайте ежедневные доходы, расходы и фото чеков для контроля бюджета.",
    addTransactionAction = "Добавить операцию",
    editTransactionTitle = "Изменить операцию",
    deleteTxConfirmTitle = "Удалить операцию?",
    deleteTxConfirmBody = { "Запись «$it» будет удалена из истории финансов." },
    amountLabel = "Сумма (Rp)",
    amountReadAs = { "Сумма: $it" },
    txTitleLabel = "Название / Описание",
    txExpensePlaceholder = "Напр.: Продукты, Утренний кофе",
    txIncomePlaceholder = "Напр.: Зарплата, Оплата проекта",
    categoryLabel = "Категория",
    attachReceipt = "Фото чека",
    changeReceipt = "Сменить чек",
    optionalNoteLabel = "Доп. заметка (необяз.)",
    amountZeroError = "Введите сумму больше Rp 0",
    saveTransactionPrefix = { "Сохранить ($it)" },
    activitiesTitle = "Активность",
    activitiesSubtitle = "Спорт, привычки и рутина",
    quickAddLabel = "Быстро добавить",
    todayTargetTitle = "Цель на сегодня",
    percentAchieved = { "$it% выполнено" },
    doneFraction = { done, total -> "$done / $total Готово" },
    activeDurationLabel = "Время",
    minutesUnit = { "$it мин" },
    bestStreakLabel = "Лучшая серия",
    daysUnit = { "$it дн." },
    streakShort = { "${it}д" },
    emptyActivitiesFilterTitle = "Нет совпадений",
    emptyActivitiesTitle = "Пока нет активностей",
    emptyActivitiesFilterSubtitle = "Выберите другой статус или категорию.",
    emptyActivitiesSubtitle = "Отмечайте тренировки, чтение и привычки каждый день для поддержания серии!",
    addActivityAction = "Добавить активность",
    editActivityTitle = "Изменить активность",
    activityNameLabel = "Название",
    activityNamePlaceholder = "Напр.: Пробежка, Зал, Чтение",
    targetDurationLabel = "Длительность (мин, 0 если без цели)",
    minutesSuffix = "мин",
    activityNotePlaceholder = "Напр.: 5 км в парке, 20 страниц",
    attachActivityPhoto = "Фото",
    changePhoto = "Сменить фото",
    doneTodayLabel = "Выполнено сегодня",
    activityTitleRequiredError = "Введите название активности",
    saveActivityButton = "Сохранить активность",
    deleteActivityConfirmTitle = "Удалить активность?",
    deleteActivityConfirmBody = { "Активность «$it» и её история будут удалены." },
    markDoneToday = "Отметить на сегодня",
    markUndoneToday = "Снять отметку",
    backupCenterTitle = "Экспорт PDF и архив",
    localOnlyTitle = "Локальное хранение",
    localOnlyDesc = "Облако отключено. Все данные хранятся локально и готовы к экспорту в PDF или архив ZIP.",
    notesAndActivitiesStat = "Заметки и дела",
    financeTxStat = "Операции",
    photosStat = { "Фото ($it)" },
    pendingExportStat = "Не в архиве",
    pdfCardTitle = "Экспорт в PDF (.pdf)",
    pdfCardDesc = "Экспортируйте заметки с фото, финансовый отчёт с чеками и активность в один PDF-документ.",
    exportPdfAction = "Экспорт в PDF (.pdf)",
    processing = "Обработка…",
    zipCardTitle = "Резервная копия (.zip)",
    zipLastBackup = { "Последний архив ZIP: $it" },
    zipEmptyDesc = "Сохраните ZIP-архив для переноса или восстановления данных.",
    exportZipAction = "Экспорт ZIP",
    restoreZipAction = "Восстановить",
    settingsTitle = "Настройки",
    settingsSubtitle = "Язык, шрифт, тема, размер текста и кнопок",
    languageSettingLabel = "Язык",
    fontFamilyLabel = "Шрифт",
    fontSizeLabel = "Размер шрифта",
    buttonSizeLabel = "Размер кнопок",
    themeModeLabel = "Режим темы",
    colorPaletteLabel = "Цветовая палитра",
    backgroundLabel = "Фон",
    defaultBgNote = "Используется стандартный фон светлой или тёмной темы.",
    pickBgPhoto = "Выбрать фото из галереи",
    changeBgPhoto = "Сменить фото фона",
    readabilityOverlay = { "Затемнение фона ($it%)" },
    openRemindersDesc = "Уведомления и напоминания",
    remindersTitle = "Уведомления и напоминания",
    remindersSubtitle = "Напоминания для заметок, финансов и привычек",
    notificationChannelName = "Напоминания Note All",
    notificationChannelDesc = "Уведомления о заметках, финансах и ежедневной активности",
    notifPermissionMissingTitle = "Нужно разрешение на уведомления",
    notifPermissionMissingBody = "Разрешите уведомления, чтобы напоминания приходили вовремя.",
    allowNotificationsAction = "Разрешить",
    notifPermissionActiveLabel = "Уведомления включены",
    testNotificationButton = "Тест",
    testNotificationSampleTitle = "Уведомления Note All активны",
    testNotificationSampleBody = "Проверка прошла успешно! Напоминания готовы к работе.",
    testNotificationSentToast = "Тестовое уведомление отправлено",
    routineRemindersSection = "Ежедневные напоминания",
    presetFinanceReminderTitle = "Запишите финансы за день",
    presetFinanceReminderBody = "Не забудьте внести сегодняшние доходы и расходы в Note All.",
    presetActivitiesReminderTitle = "Проверьте активность дня",
    presetActivitiesReminderBody = "Пора выполнить дневные привычки и поддержать серию!",
    customRemindersSection = "Напоминания по расписанию",
    addReminderButton = "Добавить",
    noCustomRemindersYet = "Пока нет напоминаний. Нажмите + Добавить, чтобы задать время.",
    reminderTitleLabel = "Название напоминания",
    reminderTitlePlaceholder = "Напр.: Оплатить счёт, Встреча, Витамины",
    reminderMessageLabel = "Текст уведомления (необяз.)",
    reminderMessagePlaceholder = "Детали, которые будут в уведомлении…",
    quickLinkFromNotes = "Из заметок",
    reminderFrequencyLabel = "Повтор",
    reminderOnceLabel = "Один раз",
    reminderDailyLabel = "Ежедневно",
    reminderDateLabel = "Дата",
    reminderTimeLabel = "Время",
    reminderTargetTabLabel = "Открыть раздел",
    saveReminderAction = "Сохранить",
    reminderTitleRequiredError = "Введите название напоминания",
    reminderPastTimeError = "Выберите дату и время в будущем",
    reminderSavedMessage = { "Напоминание задано на $it" },
    reminderDeletedMessage = "Напоминание удалено",
    reminderDailyPrefix = "Ежедневно",
    reminderOncePrefix = "Разово",
    reminderStatusTriggered = "Сработало",
    defaultReminderNotificationBody = "Нажмите, чтобы открыть напоминание в Note All.",
    deleteReminderDesc = "Удалить напоминание",
    pickTimeDialogTitle = "Выберите время",
    confirmAction = "Выбрать",
    financeChartButton = "График доходов и расходов",
    financeChartTitle = "График финансов",
    financeChartSubtitle = "Сравнение доходов и расходов",
    chartBarModeLabel = "Столбцы",
    chartDonutModeLabel = "Доли",
    chartTapHint = "Нажмите на столбец для просмотра сумм",
    chartEmptyMessage = "За выбранный период нет операций для графика.",
    chartWeekPrefix = "Н",
    chartCashflowShareTitle = "Соотношение потоков",
    chartTopExpensesTitle = "Структура расходов",
    chartTotalLabel = "Всего",
    folderActionChip = "Папки",
    manageFoldersTitle = "Категории и папки заметок",
    manageFoldersSubtitle = "Создавайте, переименовывайте и удаляйте папки для порядка",
    createFolderFieldLabel = "Название новой папки",
    createFolderFieldPlaceholder = "Напр.: Учёба, Рецепты, Проекты",
    createFolderSubmit = "Создать",
    renameFolderTitle = "Переименовать папку",
    deleteFolderTitle = "Удалить папку?",
    deleteFolderConfirmBody = { name, count ->
        if (count > 0) {
            "Папка «$name» будет удалена, а $count заметок переместятся в раздел «Общее»."
        } else {
            "Папка «$name» будет удалена из списка категорий."
        }
    },
    folderNoteCount = { "$it зам." },
    defaultFolderBadge = "Основная",
    folderEmptyNameError = "Введите название папки",
    folderTooLongError = "Максимум 32 символа",
    folderDuplicateError = "Такая папка уже существует",
    folderProtectedError = "Основную папку нельзя изменить",
    folderCreatedMessage = { "Папка «$it» создана" },
    folderRenamedMessage = { "Папка переименована в «$it»" },
    folderDeletedMessage = { "Папка «$it» удалена" },
    renameFolderDesc = "Переименовать папку",
    deleteFolderDesc = "Удалить папку",
    addFolderDialogTitle = "Новая папка"
)

fun mandarinStrings() = AppStrings(
    tabNotes = "笔记",
    tabFinance = "财务",
    tabActivities = "日常",
    justNow = "刚刚",
    minutesAgo = { "${it}分钟前" },
    hoursAgo = { "${it}小时前" },
    yesterday = "昨天",
    cancel = "取消",
    delete = "删除",
    saveChanges = "保存更改",
    select = "选择",
    close = "关闭",
    reset = "重置",
    showAll = "显示全部",
    clearSearch = "清除搜索",
    back = "返回",
    pdfChip = "PDF",
    exportPdfShort = "导出 PDF",
    openSettingsDesc = "设置语言、字体、主题与按钮大小",
    notesStatsSummary = { notes, photos -> "$notes 条笔记 • $photos 张照片" },
    localNoticeCompact = "本地存储 • 导出 PDF",
    searchNotesHint = "搜索标题、内容或标签…",
    toggleToList = "切换为列表视图",
    toggleToGrid = "切换为网格视图",
    emptyNotesTitle = "暂无笔记",
    emptyNotesSubtitle = "记录您的灵感、待办或重要照片。所有内容安全保存在本机并支持随时导出。",
    emptySearchTitle = "无匹配结果",
    emptySearchSubtitle = "请尝试其他关键词或分类筛选。",
    createNoteButton = "新建笔记",
    newNoteAction = "新建笔记",
    deleteNoteConfirmTitle = "删除此笔记？",
    deleteNoteConfirmBody = "该笔记及其所有附加照片将从设备中永久删除。",
    photoNoteFallback = "图片笔记",
    statusBackedUp = "已备份",
    statusLocal = "本地",
    pinNote = "置顶",
    unpinNote = "取消置顶",
    deleteNoteAction = "删除笔记",
    autoSaved = "已自动保存",
    saving = "正在保存…",
    editorTitleHint = "笔记标题…",
    editorBodyHint = "在此输入内容…",
    gallery = "相册",
    camera = "相机",
    photoCount = { "$it 张照片" },
    shareNote = "分享",
    exportNotePdfDesc = "导出笔记为 PDF",
    removeImageDesc = "移除图片",
    fullscreenPhotoDesc = { "全屏查看第 $it 张照片" },
    deleteImageDesc = "删除此照片",
    financeTitle = "财务",
    cashFlowPrefix = "收支",
    netBalanceTitle = "净结余",
    txCountLabel = { "$it 笔交易" },
    incomeLabel = "收入",
    expenseLabel = "支出",
    expensePerCategory = "分类支出占比",
    searchFinanceHint = "搜索交易或分类…",
    financeEmptyTitle = "暂无财务记录",
    financeEmptySubtitle = "记录每日收支并附上票据照片，轻松掌握现金流。",
    addTransactionAction = "记一笔",
    editTransactionTitle = "编辑交易",
    deleteTxConfirmTitle = "删除此交易？",
    deleteTxConfirmBody = { "交易记录“$it”将从财务历史中移除。" },
    amountLabel = "金额 (Rp)",
    amountReadAs = { "金额：$it" },
    txTitleLabel = "标题 / 说明",
    txExpensePlaceholder = "例如：买菜、早咖啡",
    txIncomePlaceholder = "例如：月薪、项目款",
    categoryLabel = "分类",
    attachReceipt = "票据照片",
    changeReceipt = "更换票据",
    optionalNoteLabel = "备注（可选）",
    amountZeroError = "请输入大于 Rp 0 的金额",
    saveTransactionPrefix = { "保存$it" },
    activitiesTitle = "日常活动",
    activitiesSubtitle = "运动、习惯与打卡",
    quickAddLabel = "快速添加",
    todayTargetTitle = "今日目标",
    percentAchieved = { "已完成 $it%" },
    doneFraction = { done, total -> "$done / $total 已完成" },
    activeDurationLabel = "有效时长",
    minutesUnit = { "$it 分钟" },
    bestStreakLabel = "最长连续",
    daysUnit = { "$it 天" },
    streakShort = { "${it}天" },
    emptyActivitiesFilterTitle = "无匹配活动",
    emptyActivitiesTitle = "暂无日常活动",
    emptyActivitiesFilterSubtitle = "请尝试切换状态或分类筛选。",
    emptyActivitiesSubtitle = "开始记录运动、阅读、喝水等日常习惯，养成连续打卡记录！",
    addActivityAction = "添加活动",
    editActivityTitle = "编辑活动",
    activityNameLabel = "活动名称",
    activityNamePlaceholder = "例如：晨跑、健身、阅读",
    targetDurationLabel = "目标时长（分钟，无要求填 0）",
    minutesSuffix = "分钟",
    activityNotePlaceholder = "例如：公园跑 5 公里、阅读 20 页",
    attachActivityPhoto = "活动照片",
    changePhoto = "更换照片",
    doneTodayLabel = "今日已完成",
    activityTitleRequiredError = "请先输入活动名称",
    saveActivityButton = "保存活动",
    deleteActivityConfirmTitle = "删除此活动？",
    deleteActivityConfirmBody = { "活动“$it”及其打卡记录将被删除。" },
    markDoneToday = "标记今日已完成",
    markUndoneToday = "取消今日完成",
    backupCenterTitle = "PDF 导出与备份",
    localOnlyTitle = "本地存储已启用",
    localOnlyDesc = "未启用云同步，所有数据保存在本地，可随时导出为 PDF 文档或 ZIP 备份。",
    notesAndActivitiesStat = "笔记与活动",
    financeTxStat = "财务交易",
    photosStat = { "照片 ($it)" },
    pendingExportStat = "待备份",
    pdfCardTitle = "导出 PDF 文档 (.pdf)",
    pdfCardDesc = "将所有笔记与照片、财务收支与票据、日常活动导出为一个清晰可打印的 PDF 文件。",
    exportPdfAction = "导出为 PDF (.pdf)",
    processing = "处理中…",
    zipCardTitle = "ZIP 备份与恢复 (.zip)",
    zipLastBackup = { "上次 ZIP 备份：$it" },
    zipEmptyDesc = "保存 ZIP 压缩包以便在设备间迁移或恢复全部数据。",
    exportZipAction = "备份 ZIP",
    restoreZipAction = "恢复 ZIP",
    settingsTitle = "设置",
    settingsSubtitle = "语言、字体、主题、文字与按钮大小",
    languageSettingLabel = "语言",
    fontFamilyLabel = "字体样式",
    fontSizeLabel = "字体大小",
    buttonSizeLabel = "按钮大小",
    themeModeLabel = "主题模式",
    colorPaletteLabel = "主题配色",
    backgroundLabel = "背景样式",
    defaultBgNote = "使用随浅色/深色模式自动适配的默认背景。",
    pickBgPhoto = "从相册选择图片",
    changeBgPhoto = "更换背景图片",
    readabilityOverlay = { "背景遮罩 ($it%)" },
    openRemindersDesc = "通知与定时提醒",
    remindersTitle = "通知与提醒",
    remindersSubtitle = "为笔记、财务与日常习惯设置提醒",
    notificationChannelName = "Note All 提醒",
    notificationChannelDesc = "笔记、财务记账与日常活动的定时通知提醒",
    notifPermissionMissingTitle = "尚未开启通知权限",
    notifPermissionMissingBody = "请允许通知权限，以便提醒能够准时响铃并弹出。",
    allowNotificationsAction = "允许通知",
    notifPermissionActiveLabel = "通知已开启",
    testNotificationButton = "测试通知",
    testNotificationSampleTitle = "Note All 通知已就绪",
    testNotificationSampleBody = "通知测试成功！您的提醒将按时送达。",
    testNotificationSentToast = "已发送测试通知",
    routineRemindersSection = "每日固定提醒",
    presetFinanceReminderTitle = "记录今日财务收支",
    presetFinanceReminderBody = "别忘了在 Note All 中记录今天的收入和支出。",
    presetActivitiesReminderTitle = "今日日常打卡提醒",
    presetActivitiesReminderBody = "该完成今天的运动或习惯目标，继续保持连续记录！",
    customRemindersSection = "日程与笔记提醒",
    addReminderButton = "添加",
    noCustomRemindersYet = "暂无自定义提醒。点击“+ 添加”即可为笔记或事项设置提醒。",
    reminderTitleLabel = "提醒标题",
    reminderTitlePlaceholder = "例如：交水电费、团队会议、吃维生素",
    reminderMessageLabel = "通知内容（可选）",
    reminderMessagePlaceholder = "显示在通知栏中的补充说明…",
    quickLinkFromNotes = "关联笔记",
    reminderFrequencyLabel = "重复方式",
    reminderOnceLabel = "单次",
    reminderDailyLabel = "每天",
    reminderDateLabel = "日期",
    reminderTimeLabel = "时间",
    reminderTargetTabLabel = "打开页面",
    saveReminderAction = "保存提醒",
    reminderTitleRequiredError = "请先输入提醒标题",
    reminderPastTimeError = "请选择未来的日期和时间",
    reminderSavedMessage = { "已设置 $it 的提醒" },
    reminderDeletedMessage = "已删除提醒",
    reminderDailyPrefix = "每天",
    reminderOncePrefix = "单次",
    reminderStatusTriggered = "已触发",
    defaultReminderNotificationBody = "点击打开 Note All 查看您的提醒。",
    deleteReminderDesc = "删除提醒",
    pickTimeDialogTitle = "选择提醒时间",
    confirmAction = "确定",
    financeChartButton = "收支对比图表",
    financeChartTitle = "财务收支图表",
    financeChartSubtitle = "收入与支出可视化分析",
    chartBarModeLabel = "柱状图",
    chartDonutModeLabel = "占比图",
    chartTapHint = "点击柱状条查看具体收支金额",
    chartEmptyMessage = "当前时间段暂无交易记录可生成图表。",
    chartWeekPrefix = "第",
    chartCashflowShareTitle = "收支比例",
    chartTopExpensesTitle = "支出分类占比",
    chartTotalLabel = "总计",
    folderActionChip = "文件夹",
    manageFoldersTitle = "笔记分类与文件夹",
    manageFoldersSubtitle = "创建、重命名或删除文件夹，让笔记井井有条",
    createFolderFieldLabel = "新文件夹 / 分类名称",
    createFolderFieldPlaceholder = "例如：学习、食谱、项目",
    createFolderSubmit = "创建",
    renameFolderTitle = "重命名文件夹",
    deleteFolderTitle = "删除此文件夹？",
    deleteFolderConfirmBody = { name, count ->
        if (count > 0) {
            "文件夹“$name”将被删除，其中的 $count 条笔记会安全移动至“通用”。"
        } else {
            "文件夹“$name”将从分类列表中移除。"
        }
    },
    folderNoteCount = { "$it 条笔记" },
    defaultFolderBadge = "默认",
    folderEmptyNameError = "请先输入文件夹名称",
    folderTooLongError = "最多 32 个字符",
    folderDuplicateError = "该文件夹已存在",
    folderProtectedError = "默认文件夹不可修改",
    folderCreatedMessage = { "已创建文件夹“$it”" },
    folderRenamedMessage = { "文件夹已重命名为“$it”" },
    folderDeletedMessage = { "已删除文件夹“$it”" },
    renameFolderDesc = "重命名文件夹",
    deleteFolderDesc = "删除文件夹",
    addFolderDialogTitle = "新建文件夹"
)
