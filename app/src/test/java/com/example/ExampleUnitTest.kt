package com.example

import com.example.data.AppearanceSettings
import com.example.data.NoteCategoryEntity
import com.example.data.NoteEntity
import com.example.data.ReminderEntity
import com.example.data.ReminderTargetTab
import com.example.data.TransactionEntity
import com.example.data.TransactionType
import com.example.data.mergeNoteCategories
import com.example.ui.FinancePeriod
import com.example.ui.buildFinanceChartBuckets
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.stringsForLanguage
import com.example.ui.theme.AppFontChoice
import com.example.ui.theme.AppFontScale
import com.example.ui.theme.buildAppTypography
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun appearanceSettings_and_fontChoices_initializeWithoutCircularError() {
    val defaults = AppearanceSettings()
    assertEquals(AppFontChoice.PLUS_JAKARTA, defaults.fontChoice)
    assertEquals(AppLanguage.INDONESIAN, defaults.language)

    AppFontChoice.entries.forEach { choice ->
      assertNotNull(choice.fontFamily)
      val typography = buildAppTypography(choice, AppFontScale.NORMAL)
      assertNotNull(typography.bodyLarge.fontFamily)
    }
  }

  @Test
  fun languageSetting_supportsIndonesianEnglishRussianAndMandarin() {
    assertEquals(4, AppLanguage.entries.size)
    assertEquals(AppLanguage.INDONESIAN, AppLanguage.fromKey("ID"))
    assertEquals(AppLanguage.ENGLISH, AppLanguage.fromKey("EN"))
    assertEquals(AppLanguage.RUSSIAN, AppLanguage.fromKey("RU"))
    assertEquals(AppLanguage.MANDARIN, AppLanguage.fromKey("ZH"))
    assertEquals(AppLanguage.INDONESIAN, AppLanguage.fromKey("UNKNOWN"))

    val tabNotesSet = AppLanguage.entries.map { stringsForLanguage(it).tabNotes }.toSet()
    assertEquals(4, tabNotesSet.size)
    assertEquals("Catatan", stringsForLanguage(AppLanguage.INDONESIAN).tabNotes)
    assertEquals("Notes", stringsForLanguage(AppLanguage.ENGLISH).tabNotes)
    assertEquals("Заметки", stringsForLanguage(AppLanguage.RUSSIAN).tabNotes)
    assertEquals("笔记", stringsForLanguage(AppLanguage.MANDARIN).tabNotes)

    val reminderTitles = AppLanguage.entries.map { stringsForLanguage(it).remindersTitle }.toSet()
    assertEquals(4, reminderTitles.size)

    val chartTitles = AppLanguage.entries.map { stringsForLanguage(it).financeChartTitle }.toSet()
    assertEquals(4, chartTitles.size)

    val folderTitles = AppLanguage.entries.map { stringsForLanguage(it).manageFoldersTitle }.toSet()
    assertEquals(4, folderTitles.size)
    assertEquals("Important", stringsForLanguage(AppLanguage.ENGLISH).categoryLabel("Penting"))
    assertEquals("Важное", stringsForLanguage(AppLanguage.RUSSIAN).categoryLabel("Penting"))
    assertEquals("重要", stringsForLanguage(AppLanguage.MANDARIN).categoryLabel("Penting"))
    assertEquals("Kuliah", stringsForLanguage(AppLanguage.ENGLISH).categoryLabel("Kuliah"))
  }

  @Test
  fun reminderEntity_computesFutureTriggerCorrectly() {
    val now = Calendar.getInstance().apply {
      set(2026, Calendar.NOVEMBER, 21, 14, 30, 0)
      set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    // Earlier time today rolls to tomorrow
    val nextMorning = ReminderEntity.computeNextDailyTriggerMillis(hour = 8, minute = 0, nowMillis = now)
    assertTrue(nextMorning > now)
    val morningCal = Calendar.getInstance().apply { timeInMillis = nextMorning }
    assertEquals(22, morningCal.get(Calendar.DAY_OF_MONTH))
    assertEquals(8, morningCal.get(Calendar.HOUR_OF_DAY))
    assertEquals(0, morningCal.get(Calendar.MINUTE))

    // Later time today stays today
    val tonight = ReminderEntity.computeNextDailyTriggerMillis(hour = 20, minute = 15, nowMillis = now)
    assertTrue(tonight > now)
    val tonightCal = Calendar.getInstance().apply { timeInMillis = tonight }
    assertEquals(21, tonightCal.get(Calendar.DAY_OF_MONTH))
    assertEquals(20, tonightCal.get(Calendar.HOUR_OF_DAY))
    assertEquals(15, tonightCal.get(Calendar.MINUTE))

    // UTC date picker + local time combines cleanly
    val utcDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
      clear()
      set(2026, Calendar.DECEMBER, 5)
    }.timeInMillis
    val combined = ReminderEntity.combineUtcDateAndLocalTime(utcDate, 9, 45)
    val combinedCal = Calendar.getInstance().apply { timeInMillis = combined }
    assertEquals(2026, combinedCal.get(Calendar.YEAR))
    assertEquals(Calendar.DECEMBER, combinedCal.get(Calendar.MONTH))
    assertEquals(5, combinedCal.get(Calendar.DAY_OF_MONTH))
    assertEquals(9, combinedCal.get(Calendar.HOUR_OF_DAY))
    assertEquals(45, combinedCal.get(Calendar.MINUTE))

    assertEquals(ReminderTargetTab.FINANCE, ReminderTargetTab.fromKey("FINANCE"))
  }

  @Test
  fun financeChartBuckets_aggregateIncomeAndExpenseCorrectly() {
    val now = Calendar.getInstance().apply {
      set(2026, Calendar.NOVEMBER, 21, 12, 0, 0)
      set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val tx1 = TransactionEntity(
      id = "t1",
      type = TransactionType.INCOME.key,
      amount = 500_000L,
      title = "Gaji",
      category = "Gaji",
      transactionDate = now,
      createdAt = now,
      updatedAt = now
    )
    val tx2 = TransactionEntity(
      id = "t2",
      type = TransactionType.EXPENSE.key,
      amount = 150_000L,
      title = "Makan",
      category = "Makan",
      transactionDate = now,
      createdAt = now,
      updatedAt = now
    )

    val buckets7d = buildFinanceChartBuckets(
      period = FinancePeriod.LAST_7_DAYS,
      transactions = listOf(tx1, tx2),
      locale = Locale.forLanguageTag("id-ID"),
      nowMillis = now
    )
    assertEquals(7, buckets7d.size)
    assertEquals(500_000L, buckets7d.last().income)
    assertEquals(150_000L, buckets7d.last().expense)

    val bucketsMonth = buildFinanceChartBuckets(
      period = FinancePeriod.THIS_MONTH,
      transactions = listOf(tx1, tx2),
      locale = Locale.forLanguageTag("id-ID"),
      weekPrefix = "M",
      nowMillis = now
    )
    assertEquals(5, bucketsMonth.size)
    // Day 21 is in week index 2 -> "M3"
    assertEquals("M3", bucketsMonth[2].label)
    assertEquals(500_000L, bucketsMonth[2].income)
    assertEquals(150_000L, bucketsMonth[2].expense)
  }

  @Test
  fun mergeNoteCategories_preservesDefaultFirstAndDeduplicatesCaseInsensitively() {
    val saved = listOf(
      NoteCategoryEntity(name = "Kerja", createdAt = 100L, isDefault = true),
      NoteCategoryEntity(name = "Kuliah", createdAt = 200L, isDefault = false),
      NoteCategoryEntity(name = "kuliah", createdAt = 300L, isDefault = false)
    )
    val noteCats = listOf("", " ", "Resep", "kerja", "Penting")

    val merged = mergeNoteCategories(saved, noteCats)
    assertEquals(NoteEntity.DEFAULT_CATEGORY, merged.first())
    assertEquals(listOf("Umum", "Kerja", "Kuliah", "Resep", "Penting"), merged)
  }
}
