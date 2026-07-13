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
    private const val KEY_DEFAULT_TIP_AMOUNT = "default_tip_amount"
    private const val KEY_DOUBLE_TAP_TO_LIKE = "double_tap_to_like"
    private const val KEY_SHOW_IMAGE_PREVIEW = "show_image_preview"
    private const val KEY_AUTO_PLAY_VIDEO = "auto_play_video"
    private const val KEY_FEED_SOUND_EFFECTS = "feed_sound_effects"

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

    fun getDefaultTipAmount(): Int = prefs.getInt(KEY_DEFAULT_TIP_AMOUNT, 10)
    fun setDefaultTipAmount(amount: Int) = prefs.edit().putInt(KEY_DEFAULT_TIP_AMOUNT, amount).apply()

    fun isDoubleTapToLikeEnabled(): Boolean = prefs.getBoolean(KEY_DOUBLE_TAP_TO_LIKE, true)
    fun setDoubleTapToLikeEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_DOUBLE_TAP_TO_LIKE, enabled).apply()

    fun isShowImagePreviewEnabled(): Boolean = prefs.getBoolean(KEY_SHOW_IMAGE_PREVIEW, true)
    fun setShowImagePreviewEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SHOW_IMAGE_PREVIEW, enabled).apply()

    fun isAutoPlayVideoEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_PLAY_VIDEO, false)
    fun setAutoPlayVideoEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_AUTO_PLAY_VIDEO, enabled).apply()

    fun isFeedSoundEffectsEnabled(): Boolean = prefs.getBoolean(KEY_FEED_SOUND_EFFECTS, true)
    fun setFeedSoundEffectsEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_FEED_SOUND_EFFECTS, enabled).apply()
}
