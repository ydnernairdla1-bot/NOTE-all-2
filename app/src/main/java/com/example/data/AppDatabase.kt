package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        NoteEntity::class,
        TransactionEntity::class,
        ActivityEntity::class,
        ReminderEntity::class,
        NoteCategoryEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun transactionDao(): TransactionDao
    abstract fun activityDao(): ActivityDao
    abstract fun reminderDao(): ReminderDao
    abstract fun noteCategoryDao(): NoteCategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `transactions` (
                        `id` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `amount` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `note` TEXT NOT NULL,
                        `receiptImageName` TEXT,
                        `transactionDate` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `backedUpAt` INTEGER,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `activities` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `targetMinutes` INTEGER NOT NULL,
                        `note` TEXT NOT NULL,
                        `photoImageName` TEXT,
                        `completedDatesJson` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `backedUpAt` INTEGER,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `reminders` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `message` TEXT NOT NULL,
                        `targetTab` TEXT NOT NULL,
                        `linkedNoteId` TEXT,
                        `hour` INTEGER NOT NULL,
                        `minute` INTEGER NOT NULL,
                        `isDaily` INTEGER NOT NULL,
                        `triggerAtMillis` INTEGER NOT NULL,
                        `isEnabled` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                createAndSeedCategoriesTable(db)
            }
        }

        private fun createAndSeedCategoriesTable(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `note_categories` (
                    `name` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    `isDefault` INTEGER NOT NULL,
                    PRIMARY KEY(`name`)
                )
                """.trimIndent()
            )
            val now = System.currentTimeMillis()
            NoteEntity.AVAILABLE_CATEGORIES.forEachIndexed { index, name ->
                db.execSQL(
                    "INSERT OR IGNORE INTO `note_categories` (`name`, `createdAt`, `isDefault`) VALUES (?, ?, 1)",
                    arrayOf<Any>(name, now + index)
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cloudnote_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            val now = System.currentTimeMillis()
                            NoteEntity.AVAILABLE_CATEGORIES.forEachIndexed { index, name ->
                                db.execSQL(
                                    "INSERT OR IGNORE INTO `note_categories` (`name`, `createdAt`, `isDefault`) VALUES (?, ?, 1)",
                                    arrayOf<Any>(name, now + index)
                                )
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
