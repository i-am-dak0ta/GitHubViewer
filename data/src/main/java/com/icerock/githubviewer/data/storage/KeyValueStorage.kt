package com.icerock.githubviewer.data.storage

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class KeyValueStorage @Inject constructor(@ApplicationContext context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var authToken: String?
        get() = sharedPreferences.getString(AUTH_TOKEN, null)
        set(value) {
            sharedPreferences.edit().putString(AUTH_TOKEN, value).apply()
        }

    var isAuthorized: Boolean
        get() = sharedPreferences.getBoolean(KEY_IS_AUTHORIZED, false)
        set(value) {
            sharedPreferences.edit().putBoolean(KEY_IS_AUTHORIZED, value).apply()
        }

    var login: String?
        get() = sharedPreferences.getString(KEY_LOGIN, null)
        set(value) {
            sharedPreferences.edit().putString(KEY_LOGIN, value).apply()
        }

    companion object {
        private const val PREF_NAME = "github_viewer_prefs"
        private const val KEY_IS_AUTHORIZED = "is_authorized"
        private const val AUTH_TOKEN = "auth_token"
        private const val KEY_LOGIN = "login"
    }
}