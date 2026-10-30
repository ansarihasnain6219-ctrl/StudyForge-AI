package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "uploaded_notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val mimeType: String,
    val fileSizeBytes: Long,
    val board: String,
    val studentClass: Int,
    val subject: String,
    val chapter: String,
    val extractedText: String,
    val completenessWarning: String?,
    val summary: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "question_sets")
data class QuestionSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val noteId: Long?,
    val title: String,
    val board: String,
    val studentClass: Int,
    val subject: String,
    val chapter: String,
    val questionFormatId: String,
    val difficultyId: String,
    val outputLanguageId: String,
    val questionCount: Int,
    val questionsJson: String,
    val incompleteWarning: String?,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "test_attempts")
data class TestAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionSetId: Long,
    val setTitle: String,
    val board: String,
    val studentClass: Int,
    val subject: String,
    val chapter: String,
    val totalAutoGradable: Int,
    val correctCount: Int,
    val totalQuestions: Int,
    val percentage: Float,
    val answersJson: String,
    val attemptedAt: Long = System.currentTimeMillis()
)

@Dao
interface StudyForgeDao {
    @Query("SELECT * FROM uploaded_notes ORDER BY createdAt DESC")
    fun observeAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Query("DELETE FROM uploaded_notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: Long)

    @Query("DELETE FROM uploaded_notes")
    suspend fun deleteAllNotes()

    @Query("SELECT * FROM question_sets ORDER BY createdAt DESC")
    fun observeAllQuestionSets(): Flow<List<QuestionSetEntity>>

    @Query("SELECT * FROM question_sets WHERE id = :setId LIMIT 1")
    suspend fun getQuestionSetById(setId: Long): QuestionSetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestionSet(questionSet: QuestionSetEntity): Long

    @Query("DELETE FROM question_sets WHERE id = :setId")
    suspend fun deleteQuestionSetById(setId: Long)

    @Query("DELETE FROM question_sets")
    suspend fun deleteAllQuestionSets()

    @Query("SELECT * FROM test_attempts ORDER BY attemptedAt DESC")
    fun observeAllAttempts(): Flow<List<TestAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: TestAttemptEntity): Long

    @Query("DELETE FROM test_attempts WHERE id = :attemptId")
    suspend fun deleteAttemptById(attemptId: Long)

    @Query("DELETE FROM test_attempts")
    suspend fun deleteAllAttempts()
}

@Database(
    entities = [NoteEntity::class, QuestionSetEntity::class, TestAttemptEntity::class],
    version = 1,
    exportSchema = false
)
abstract class StudyForgeDatabase : RoomDatabase() {
    abstract fun studyForgeDao(): StudyForgeDao

    companion object {
        @Volatile
        private var INSTANCE: StudyForgeDatabase? = null

        fun getInstance(context: Context): StudyForgeDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StudyForgeDatabase::class.java,
                    "studyforge_ai_db"
                ).fallbackToDestructiveMigration(true).build().also { INSTANCE = it }
            }
        }
    }
}
