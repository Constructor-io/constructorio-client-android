package io.constructor.core

import android.annotation.SuppressLint
import android.content.Context
import io.constructor.BuildConfig
import io.constructor.data.DataManager
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.model.SuggestionViewModel
import io.constructor.injection.component.AppComponent
import io.constructor.injection.component.DaggerAppComponent
import io.constructor.injection.module.AppModule
import io.constructor.injection.module.NetworkModule
import io.constructor.util.broadcastIntent
import io.constructor.util.d
import io.constructor.util.e
import io.constructor.util.urlEncode
import java.util.*

@SuppressLint("StaticFieldLeak")
object ConstructorIo {

    private lateinit var dataManager: DataManager
    private lateinit var preferenceHelper: PreferencesHelper
    private lateinit var context: Context

    internal val component: AppComponent by lazy {
        DaggerAppComponent.builder()
                .appModule(AppModule(context))
                .networkModule(NetworkModule(context))
                .build()
    }

    fun init(context: Context?, apiKey: String) {
        if (context == null) {
            throw IllegalStateException("context is null, please init library using ConstructorIo.with(context)")
        }
        this.context = context.applicationContext
        dataManager = component.dataManager()
        preferenceHelper = component.preferenceHelper()
        preferenceHelper.saveToken(apiKey)
        if (preferenceHelper.getId().isBlank()) {
            preferenceHelper.saveId(UUID.randomUUID().toString())
        }
    }

    internal fun testInit(context: Context?, apiKey: String, dataManager: DataManager, preferenceHelper: PreferencesHelper) {
        if (context == null) {
            throw IllegalStateException("Context is null, please init library using ConstructorIo.with(context)")
        }
        this.context = context.applicationContext
        this.dataManager = dataManager
        this.preferenceHelper = preferenceHelper
        preferenceHelper.saveToken(apiKey)
        if (preferenceHelper.getId().isBlank()) {
            preferenceHelper.saveId(UUID.randomUUID().toString())
        }
    }

    internal fun getAutocompleteResults(query: String) = dataManager.getAutocompleteResults(query)

    internal fun triggerSelectEvent(query: String, suggestion: SuggestionViewModel) {
        val sessionId = preferenceHelper.getSessionId()
        val userId = preferenceHelper.getId()
        val encodedParams: ArrayList<Pair<String, String>> = arrayListOf()
        suggestion.group?.groupId?.let { encodedParams.add(Constants.QueryConstants.GROUP_ID.urlEncode() to it) }
        suggestion.group?.displayName?.let { encodedParams.add(Constants.QueryConstants.GROUP_DISPLAY_NAME.urlEncode() to it.urlEncode()) }
        dataManager.triggerSelectEvent(suggestion.term,
                arrayOf(Constants.QueryConstants.SESSION to sessionId.toString(),
                        Constants.QueryConstants.IDENTITY to userId,
                        Constants.QueryConstants.AUTOCOMPLETE_SECTION to suggestion.section!!,
                        Constants.QueryConstants.ORIGINAL_QUERY to query,
                        Constants.QueryConstants.EVENT to Constants.QueryValues.EVENT_CLICK,
                        Constants.QueryConstants.CLIENT to BuildConfig.CLIENT_VERSION),
                encodedParams.toTypedArray())
                .subscribe({ response ->
                    if (response.isSuccessful) {
                        d("trigger select success") //To change body of created functions use File | Settings | File Templates.
                        context.broadcastIntent(Constants.EVENT_QUERY_SENT, Constants.EXTRA_TERM to query)
                    }
                }, { t ->
                    t.printStackTrace()
                    e("trigger select error: ${t.message}") //To change body of created functions use File | Settings | File Templates.
                })

    }

    internal fun triggerSearchEvent(query: String, suggestion: SuggestionViewModel) {
        val sessionId = preferenceHelper.getSessionId()
        val userId = preferenceHelper.getId()
        val encodedParams: ArrayList<Pair<String, String>> = arrayListOf()
        suggestion.group?.groupId?.let { encodedParams.add(Constants.QueryConstants.GROUP_ID.urlEncode() to it) }
        suggestion.group?.displayName?.let { encodedParams.add(Constants.QueryConstants.GROUP_DISPLAY_NAME.urlEncode() to it.urlEncode()) }
        dataManager.triggerSearchEvent(suggestion.term,
                arrayOf(Constants.QueryConstants.SESSION to sessionId.toString(),
                        Constants.QueryConstants.IDENTITY to userId,
                        Constants.QueryConstants.ORIGINAL_QUERY to query,
                        Constants.QueryConstants.EVENT to Constants.QueryValues.EVENT_SEARCH,
                        Constants.QueryConstants.CLIENT to BuildConfig.CLIENT_VERSION), encodedParams.toTypedArray())
                .subscribe({
                    if (it.isSuccessful) {
                        d("trigger search success") //To change body of created functions use File | Settings | File Templates.
                        context.broadcastIntent(Constants.EVENT_QUERY_SENT, Constants.EXTRA_TERM to query)
                    }
                }, {
                    it.printStackTrace()
                    e("trigger search error: ${it.message}") //To change body of created functions use File | Settings | File Templates. }

                })
    }

}