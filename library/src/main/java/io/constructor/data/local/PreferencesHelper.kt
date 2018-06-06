package io.constructor.data.local

import android.content.Context
import android.content.SharedPreferences
import io.constructor.BuildConfig
import io.constructor.core.Constants
import io.constructor.core.ConstructorIo
import io.constructor.data.DataManager
import io.constructor.injection.ApplicationContext
import io.constructor.util.d
import javax.inject.Inject
import javax.inject.Singleton

class PreferencesHelper @Inject
constructor(@ApplicationContext context: Context, prefFileName: String = PREF_FILE_NAME) {

    private val preferences: SharedPreferences = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        preferences.edit().putString(PREF_TOKEN, token).apply()
    }

    fun getToken(): String {
        return preferences.getString(PREF_TOKEN, "")
    }

    fun getSessionId(sessionIncrementAction: ((String) -> Unit)? = null): Int {
        if (!preferences.contains(SESSION_ID)) {
            return resetSession()
        }
        val sessionTime = getLastSessionAccess()
        val timeDiff = System.currentTimeMillis() - sessionTime
        if (timeDiff > SESSION_TIME_THRESHOLD) {
            var sessionId = preferences.getInt(SESSION_ID, 1)
            preferences.edit().putInt(SESSION_ID, ++sessionId).apply()
            sessionIncrementAction?.invoke(sessionId.toString())
        }
        saveLastSessionAccess(System.currentTimeMillis())
        return preferences.getInt(SESSION_ID, 1)
    }

    private fun saveLastSessionAccess(timestamp: Long) {
        preferences.edit().putLong(SESSION_LAST_ACCESS, timestamp).apply()
    }

    internal fun getLastSessionAccess(): Long = preferences.getLong(SESSION_LAST_ACCESS, System.currentTimeMillis())

    internal fun resetSession(): Int {
        val sessionId = 1
        preferences.edit().putInt(SESSION_ID, sessionId).apply()
        return sessionId
    }

    fun saveId(id: String) {
        preferences.edit().putString(PREF_ID, id).apply()
    }

    fun getId(): String {
        return preferences.getString(PREF_ID, "")
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    companion object {

        const val PREF_TOKEN = "token"

        const val PREF_ID = "id"

        const val PREF_FILE_NAME = "constructor_pref_file"

        const val SESSION_ID = "session_id"

        const val SESSION_LAST_ACCESS = "session_last_access"

        const val SESSION_TIME_THRESHOLD = 1000 * 60 * 30
    }

}