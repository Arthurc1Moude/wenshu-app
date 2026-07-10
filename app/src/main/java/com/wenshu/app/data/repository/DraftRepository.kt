package com.wenshu.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Draft(
    val content: String = "",
    val title: String = "",
    val tags: List<String> = emptyList(),
    val imagePaths: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

class DraftRepository private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("wenshu_drafts", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveDraft(content: String, title: String, tags: List<String>, imagePaths: List<String>) {
        val draft = Draft(content, title, tags, imagePaths, System.currentTimeMillis())
        prefs.edit()
            .putString(KEY_DRAFT, gson.toJson(draft))
            .apply()
    }

    fun loadDraft(): Draft? {
        val json = prefs.getString(KEY_DRAFT, null) ?: return null
        return try {
            gson.fromJson(json, Draft::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun clearDraft() {
        prefs.edit().remove(KEY_DRAFT).apply()
    }

    fun hasDraft(): Boolean {
        return loadDraft()?.content?.isNotBlank() == true
    }

    companion object {
        private const val KEY_DRAFT = "current_draft"

        @Volatile
        private var instance: DraftRepository? = null

        fun getInstance(context: Context): DraftRepository {
            return instance ?: synchronized(this) {
                instance ?: DraftRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
