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

    var id: String
        get() = preferences.getString(PREF_ID, "")
        set(value) = preferences.edit().putString(PREF_ID, value).apply()

    var token: String
        get() = preferences.getString(PREF_TOKEN, "")
        set(value) = preferences.edit().putString(PREF_TOKEN, value).apply()

    var defaultItemSection: String
        get() = preferences.getString(PREF_DEFAULT_ITEM_SECTION, "")
        set(value) = preferences.edit().putString(PREF_DEFAULT_ITEM_SECTION, value).apply()

    var groupsShownForFirstTerm: Int
        get() = preferences.getInt(GROUPS_SHOWN_FOR_FIRST_TERM, 2)
        set(value) = preferences.edit().putInt(GROUPS_SHOWN_FOR_FIRST_TERM, value).apply()

    var lastSessionAccess: Long
        get() = preferences.getLong(SESSION_LAST_ACCESS, System.currentTimeMillis())
        set(value) = preferences.edit().putLong(SESSION_LAST_ACCESS, value).apply()


    fun getSessionId(sessionIncrementAction: ((String) -> Unit)? = null): Int {
        if (!preferences.contains(SESSION_ID)) {
            return resetSession()
        }
        val sessionTime = lastSessionAccess
        val timeDiff = System.currentTimeMillis() - sessionTime
        if (timeDiff > SESSION_TIME_THRESHOLD) {
            var sessionId = preferences.getInt(SESSION_ID, 1)
            preferences.edit().putInt(SESSION_ID, ++sessionId).apply()
            sessionIncrementAction?.invoke(sessionId.toString())
        }
        lastSessionAccess = System.currentTimeMillis()
        return preferences.getInt(SESSION_ID, 1)
    }

    internal fun resetSession(): Int {
        val sessionId = 1
        preferences.edit().putInt(SESSION_ID, sessionId).apply()
        return sessionId
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    companion object {

        const val PREF_TOKEN = "token"
        const val PREF_DEFAULT_ITEM_SECTION = "default_item_section"
        const val GROUPS_SHOWN_FOR_FIRST_TERM = "groups_shown_for_first_term"
        const val PREF_ID = "id"
        const val PREF_FILE_NAME = "constructor_pref_file"
        const val SESSION_ID = "session_id"
        const val SESSION_LAST_ACCESS = "session_last_access"
        const val SESSION_TIME_THRESHOLD = 1000 * 60 * 30
    }

}