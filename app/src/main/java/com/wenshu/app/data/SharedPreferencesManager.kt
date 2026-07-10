package com.wenshu.app.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wenshu.app.WenshuApp
import com.wenshu.app.data.model.User

object SharedPreferencesManager {

    private const val PREF_NAME = "wenshu_prefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USER = "current_user"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    private val gson = Gson()
    private val prefs: SharedPreferences by lazy {
        WenshuApp.instance.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveAuth(token: String, user: User) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER, gson.toJson(user))
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun getUser(): User? {
        val userJson = prefs.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(userJson, User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun updateUser(user: User) {
        prefs.edit()
            .putString(KEY_USER, gson.toJson(user))
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getToken() != null
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
