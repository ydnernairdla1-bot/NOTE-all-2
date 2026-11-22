package com.example

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.data.ActivityEntity
import com.example.data.BackupArchiveManager
import com.example.data.ImageStorageManager
import com.example.data.NoteEntity
import com.example.data.TransactionEntity
import com.example.data.TransactionType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Note All", appName)
  }

  @Test
  fun `backup archive roundtrip preserves notes transactions and activities`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val archiveManager = BackupArchiveManager(context, ImageStorageManager(context))
    val now = System.currentTimeMillis()

    val notes = listOf(
      NoteEntity(
        id = "n1",
        title = "Rencana Mingguan",
        content = "Baris satu.\nBaris dua.",
        category = "Kerja",
        colorKey = "DEFAULT",
        isPinned = true,
        imageNamesJson = "[]",
        createdAt = now,
        updatedAt = now
      )
    )
    val transactions = listOf(
      TransactionEntity(
        id = "t1",
        type = TransactionType.INCOME.key,
        amount = 1_500_000L,
        title = "Gaji",
        category = "Gaji",
        note = "Bulan ini",
        receiptImageName = null,
        transactionDate = now,
        createdAt = now,
        updatedAt = now
      )
    )
    val activities = listOf(
      ActivityEntity(
        id = "a1",
        title = "Lari Pagi",
        category = "Olahraga",
        targetMinutes = 30,
        note = "5 km",
        photoImageName = null,
        completedDatesJson = "[\"${ActivityEntity.todayDateKey()}\"]",
        createdAt = now,
        updatedAt = now
      )
    )

    val zipFile = File(context.cacheDir, "test_backup.zip")
    val exportRes = archiveManager.exportBackup(Uri.fromFile(zipFile), notes, transactions, activities)
    assertEquals(1, exportRes.noteCount)
    assertEquals(1, exportRes.transactionCount)
    assertEquals(1, exportRes.activityCount)
    assertTrue(zipFile.exists() && zipFile.length() > 0L)

    val imported = archiveManager.importBackup(Uri.fromFile(zipFile))
    assertEquals(1, imported.notes.size)
    assertEquals("Rencana Mingguan", imported.notes.first().title)
    assertEquals(1, imported.transactions.size)
    assertEquals(1_500_000L, imported.transactions.first().amount)
    assertEquals(1, imported.activities.size)
    assertEquals("Lari Pagi", imported.activities.first().title)

    zipFile.delete()
  }
}
