package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppUiLanguage
import com.example.model.EducationBoard
import com.example.model.OutputLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "studyforge_user_prefs")

data class UserProfilePrefs(
    val studentName: String = "",
    val isGuestMode: Boolean = true,
    val uiLanguage: AppUiLanguage = AppUiLanguage.ENGLISH,
    val outputLanguage: OutputLanguage = OutputLanguage.ENGLISH,
    val selectedBoard: EducationBoard = EducationBoard.CBSE,
    val selectedClass: Int = 10,
    val selectedSubject: String = "Science (विज्ञान)",
    val selectedChapter: String = "Chemical Reactions and Equations",
    val darkThemeEnabled: Boolean = false
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val STUDENT_NAME = stringPreferencesKey("student_name")
        val IS_GUEST_MODE = booleanPreferencesKey("is_guest_mode")
        val UI_LANGUAGE = stringPreferencesKey("ui_language")
        val OUTPUT_LANGUAGE = stringPreferencesKey("output_language")
        val SELECTED_BOARD = stringPreferencesKey("selected_board")
        val SELECTED_CLASS = intPreferencesKey("selected_class")
        val SELECTED_SUBJECT = stringPreferencesKey("selected_subject")
        val SELECTED_CHAPTER = stringPreferencesKey("selected_chapter")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
    }

    val preferencesFlow: Flow<UserProfilePrefs> = context.dataStore.data.map { prefs ->
        val uiLangCode = prefs[Keys.UI_LANGUAGE] ?: AppUiLanguage.ENGLISH.code
        val uiLang = AppUiLanguage.entries.find { it.code == uiLangCode } ?: AppUiLanguage.ENGLISH

        val outLangId = prefs[Keys.OUTPUT_LANGUAGE] ?: OutputLanguage.ENGLISH.id
        val outLang = OutputLanguage.entries.find { it.id == outLangId } ?: OutputLanguage.ENGLISH

        val boardCode = prefs[Keys.SELECTED_BOARD] ?: EducationBoard.CBSE.code
        val board = EducationBoard.entries.find { it.code == boardCode } ?: EducationBoard.CBSE

        UserProfilePrefs(
            studentName = prefs[Keys.STUDENT_NAME] ?: "",
            isGuestMode = prefs[Keys.IS_GUEST_MODE] ?: true,
            uiLanguage = uiLang,
            outputLanguage = outLang,
            selectedBoard = board,
            selectedClass = prefs[Keys.SELECTED_CLASS] ?: 10,
            selectedSubject = prefs[Keys.SELECTED_SUBJECT] ?: "Science (विज्ञान)",
            selectedChapter = prefs[Keys.SELECTED_CHAPTER] ?: "Chemical Reactions and Equations",
            darkThemeEnabled = prefs[Keys.DARK_THEME] ?: false
        )
    }

    suspend fun updateUiLanguage(lang: AppUiLanguage) {
        context.dataStore.edit { it[Keys.UI_LANGUAGE] = lang.code }
    }

    suspend fun updateOutputLanguage(lang: OutputLanguage) {
        context.dataStore.edit { it[Keys.OUTPUT_LANGUAGE] = lang.id }
    }

    suspend fun updateAcademicSelection(
        board: EducationBoard,
        studentClass: Int,
        subject: String,
        chapter: String
    ) {
        context.dataStore.edit {
            it[Keys.SELECTED_BOARD] = board.code
            it[Keys.SELECTED_CLASS] = studentClass
            it[Keys.SELECTED_SUBJECT] = subject
            it[Keys.SELECTED_CHAPTER] = chapter
        }
    }

    suspend fun saveStudentAccount(name: String) {
        context.dataStore.edit {
            it[Keys.STUDENT_NAME] = name.trim()
            it[Keys.IS_GUEST_MODE] = name.isBlank()
        }
    }

    suspend fun switchToGuestMode() {
        context.dataStore.edit {
            it[Keys.STUDENT_NAME] = ""
            it[Keys.IS_GUEST_MODE] = true
        }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_THEME] = enabled }
    }
}
