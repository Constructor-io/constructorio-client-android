package io.constructor.data.local

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import io.constructor.injection.ApplicationContext
import javax.inject.Inject

/**
 * @suppress
 */
@SuppressLint("CommitPrefEdits")
class PreferencesHelper @Inject
constructor(@ApplicationContext context: Context, prefFileName: String = PREF_FILE_NAME) {

    private val preferences: SharedPreferences = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE)

    var id: String
        get() = preferences.getString(PREF_ID, "")!!
        set(value) = preferences.edit().putString(PREF_ID, value).apply()

    var apiKey: String
        get() = preferences.getString(PREF_API_KEY, "")!!
        set(value) = preferences.edit().putString(PREF_API_KEY, value).apply()

    var defaultItemSection: String
        get() = preferences.getString(PREF_DEFAULT_ITEM_SECTION, "")!!
        set(value) = preferences.edit().putString(PREF_DEFAULT_ITEM_SECTION, value).apply()

    var groupsShownForFirstTerm: Int
        get() = preferences.getInt(PREF_GROUPS_SHOWN_FOR_FIRST_TERM, 2)
        set(value) = preferences.edit().putInt(PREF_GROUPS_SHOWN_FOR_FIRST_TERM, value).apply()

    var lastSessionAccess: Long
        get() = preferences.getLong(SESSION_LAST_ACCESS, System.currentTimeMillis())
        set(value) = preferences.edit().putLong(SESSION_LAST_ACCESS, value).apply()

    var serviceUrl: String?
        get() = preferences.getString(PREF_SERVICE_URL, "")
        set(value) = preferences.edit().putString(PREF_SERVICE_URL, value).apply()

    var port: Int
        get() = preferences.getInt(PREF_SERVICE_PORT, 443)
        set(value) = preferences.edit().putInt(PREF_SERVICE_PORT, value).apply()

    var scheme: String?
        get() = preferences.getString(PREF_SERVICE_SCHEME, "https")
        set(value) = preferences.edit().putString(PREF_SERVICE_SCHEME, value).apply()

    fun getSessionId(sessionIncrementAction: ((String) -> Unit)? = null, forceIncrement: Boolean = false): Int {
        if (!preferences.contains(SESSION_ID)) {
            return resetSession(sessionIncrementAction)
        }
        val sessionTime = lastSessionAccess
        val timeDiff = System.currentTimeMillis() - sessionTime
        if (timeDiff > SESSION_TIME_THRESHOLD || forceIncrement) {
            var sessionId = preferences.getInt(SESSION_ID, 1)
            preferences.edit().putInt(SESSION_ID, ++sessionId).apply()
            sessionIncrementAction?.invoke(sessionId.toString())
        }
        lastSessionAccess = System.currentTimeMillis()
        return preferences.getInt(SESSION_ID, 1)
    }

    internal fun resetSession(sessionIncrementAction: ((String) -> Unit)?): Int {
        val sessionId = 1
        preferences.edit().putInt(SESSION_ID, sessionId).apply()
        sessionIncrementAction?.invoke(sessionId.toString())
        return sessionId
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    companion object {
        const val PREF_API_KEY = "api key"
        const val PREF_DEFAULT_ITEM_SECTION = "default_item_section"
        const val PREF_GROUPS_SHOWN_FOR_FIRST_TERM = "groups_shown_for_first_term"
        const val PREF_ID = "id"
        const val PREF_FILE_NAME = "constructor_pref_file"
        const val SESSION_ID = "session_id"
        const val SESSION_LAST_ACCESS = "session_last_access"
        const val SESSION_TIME_THRESHOLD = 1000 * 60 * 30
        const val PREF_SERVICE_URL = "service_url"
        const val PREF_SERVICE_PORT = "service_port"
        const val PREF_SERVICE_SCHEME = "service_scheme"
    }

}