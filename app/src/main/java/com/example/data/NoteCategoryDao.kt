package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteCategoryDao {
    @Query("SELECT * FROM note_categories ORDER BY isDefault DESC, createdAt ASC, name COLLATE NOCASE ASC")
    fun observeAllCategories(): Flow<List<NoteCategoryEntity>>

    @Query("SELECT * FROM note_categories ORDER BY isDefault DESC, createdAt ASC, name COLLATE NOCASE ASC")
    suspend fun getAllCategoriesSnapshot(): List<NoteCategoryEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: NoteCategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategories(categories: List<NoteCategoryEntity>)

    @Query("DELETE FROM note_categories WHERE LOWER(name) = LOWER(:name)")
    suspend fun deleteCategoryByName(name: String)

    @Query("SELECT * FROM note_categories WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getByNameIgnoreCase(name: String): NoteCategoryEntity?
}
