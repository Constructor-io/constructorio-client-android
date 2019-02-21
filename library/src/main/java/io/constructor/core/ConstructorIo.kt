package io.constructor.core

import android.annotation.SuppressLint
import android.content.Context
import io.constructor.data.ConstructorData
import io.constructor.data.DataManager
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.model.Group
import io.constructor.data.model.Suggestion
import io.constructor.injection.component.AppComponent
import io.constructor.injection.component.DaggerAppComponent
import io.constructor.injection.module.AppModule
import io.constructor.injection.module.NetworkModule
import io.constructor.util.broadcastIntent
import io.constructor.util.d
import io.constructor.util.e
import io.constructor.util.urlEncode
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

typealias ConstructorError = ((Throwable) -> Unit)?

@SuppressLint("StaticFieldLeak")
object ConstructorIo {

    private lateinit var dataManager: DataManager
    private lateinit var preferenceHelper: PreferencesHelper
    private lateinit var configMemoryHolder: ConfigMemoryHolder
    private lateinit var context: Context
    private var disposable = CompositeDisposable()

    var userId: String?
        get() = configMemoryHolder.userId
        set(value) {
            configMemoryHolder.userId = value
        }

    internal val component: AppComponent by lazy {
        DaggerAppComponent.builder()
                .appModule(AppModule(context))
                .networkModule(NetworkModule(context))
                .build()
    }

    private var sessionIncrementEventHandler: (String) -> Unit = {
        trackSessionStartInternal(it)
    }

    private fun trackSessionStartInternal(sessionId: String, errorCallback: ConstructorError = null) {
        disposable.add(dataManager.trackSessionStart(arrayOf(Constants.QueryConstants.SESSION to sessionId,
                Constants.QueryConstants.ACTION to Constants.QueryValues.EVENT_SESSION_START)).subscribeOn(Schedulers.io()).subscribe({}, {
            errorCallback?.invoke(it)
            d("Error triggering Session Change event")
        }))
    }

    fun init(context: Context?, constructorIoConfig: ConstructorIoConfig) {
        if (context == null) {
            throw IllegalStateException("context is null, please init library using ConstructorIo.with(context)")
        }
        this.context = context.applicationContext
        dataManager = component.dataManager()
        preferenceHelper = component.preferenceHelper()
        configMemoryHolder = component.configMemoryHolder()
        configMemoryHolder.autocompleteResultCount = constructorIoConfig.autocompleteResultCount
        configMemoryHolder.testCellParams = constructorIoConfig.testCells
        preferenceHelper.token = constructorIoConfig.apiKey

        preferenceHelper.defaultItemSection = constructorIoConfig.defaultItemSection
        if (preferenceHelper.id.isBlank()) {
            preferenceHelper.id = UUID.randomUUID().toString()
        }
    }

    fun getSessionId() = preferenceHelper.getSessionId()

    fun getClientId() = preferenceHelper.id

    internal fun testInit(context: Context?, constructorIoConfig: ConstructorIoConfig, dataManager: DataManager, preferenceHelper: PreferencesHelper, configMemoryHolder: ConfigMemoryHolder) {
        if (context == null) {
            throw IllegalStateException("Context is null, please init library using ConstructorIo.with(context)")
        }
        this.context = context.applicationContext
        this.dataManager = dataManager
        this.preferenceHelper = preferenceHelper
        this.configMemoryHolder = configMemoryHolder
        preferenceHelper.token = constructorIoConfig.apiKey
        if (preferenceHelper.id.isBlank()) {
            preferenceHelper.id = UUID.randomUUID().toString()
        }
    }

    fun appMovedToForeground() {
        preferenceHelper.getSessionId(sessionIncrementEventHandler)
    }

    fun getAutocompleteResults(query: String): Observable<ConstructorData<List<Suggestion>?>> {
        val params = mutableListOf<Pair<String, String>>()
        configMemoryHolder.autocompleteResultCount?.entries?.forEach {
            params.add(Pair(Constants.QueryConstants.NUM_RESULTS+it.key, it.value.toString()))
        }
        return dataManager.getAutocompleteResults(query, params.toTypedArray())
    }

    fun trackAutocompleteSelect(searchTerm: String, originalQuery: String, sectionName: String, group: Group? = null, errorCallback: ConstructorError = null) {
        val sessionId = preferenceHelper.getSessionId(sessionIncrementEventHandler)
        val encodedParams: ArrayList<Pair<String, String>> = arrayListOf()
        group?.groupId?.let { encodedParams.add(Constants.QueryConstants.GROUP_ID.urlEncode() to it) }
        group?.displayName?.let { encodedParams.add(Constants.QueryConstants.GROUP_DISPLAY_NAME.urlEncode() to it.urlEncode()) }
        disposable.add(dataManager.trackAutocompleteSelect(searchTerm,
                arrayOf(Constants.QueryConstants.SESSION to sessionId.toString(),
                        Constants.QueryConstants.AUTOCOMPLETE_SECTION to sectionName,
                        Constants.QueryConstants.ORIGINAL_QUERY to originalQuery,
                        Constants.QueryConstants.EVENT to Constants.QueryValues.EVENT_CLICK),
                encodedParams.toTypedArray())
                .subscribe({
                    context.broadcastIntent(Constants.EVENT_QUERY_SENT, Constants.EXTRA_TERM to searchTerm)
                }, { t ->
                    t.printStackTrace()
                    errorCallback?.invoke(t)
                    e("trigger select error: ${t.message}") //To change body of created functions use File | Settings | File Templates.
                }))
    }

    fun trackSearchSubmit(searchTerm: String, originalQuery: String, group: Group?, errorCallback: ConstructorError = null) {
        val sessionId = preferenceHelper.getSessionId(sessionIncrementEventHandler)
        val encodedParams: ArrayList<Pair<String, String>> = arrayListOf()
        group?.groupId?.let { encodedParams.add(Constants.QueryConstants.GROUP_ID.urlEncode() to it) }
        group?.displayName?.let { encodedParams.add(Constants.QueryConstants.GROUP_DISPLAY_NAME.urlEncode() to it.urlEncode()) }
        disposable.add(dataManager.trackSearchSubmit(searchTerm,
                arrayOf(Constants.QueryConstants.SESSION to sessionId.toString(),
                        Constants.QueryConstants.ORIGINAL_QUERY to originalQuery,
                        Constants.QueryConstants.EVENT to Constants.QueryValues.EVENT_SEARCH), encodedParams.toTypedArray())
                .subscribe({
                    context.broadcastIntent(Constants.EVENT_QUERY_SENT, Constants.EXTRA_TERM to searchTerm)
                }, {
                    it.printStackTrace()
                    errorCallback?.invoke(it)
                    e("trigger search error: ${it.message}")
                }))
    }

    fun trackConversion(itemName: String, customerId: String, revenue: Double?, searchTerm: String = Constants.QueryConstants.TERM_UNKNOWN, sectionName: String? = null, errorCallback: ConstructorError = null) {
        val sessionId = preferenceHelper.getSessionId(sessionIncrementEventHandler)
        disposable.add(dataManager.trackConversion(searchTerm, itemName, customerId, "%.2f".format(revenue),
                arrayOf(Constants.QueryConstants.SESSION to sessionId.toString(),
                        Constants.QueryConstants.AUTOCOMPLETE_SECTION to (sectionName ?: preferenceHelper.defaultItemSection))).subscribeOn(Schedulers.io())
                .subscribe({}, { t ->
                    t.printStackTrace()
                    errorCallback?.invoke(t)
                    e("Conversion event error: ${t.message}")
                }))
    }

    fun trackSearchResultClick(itemName: String, customerId: String, searchTerm: String = Constants.QueryConstants.TERM_UNKNOWN, sectionName: String? = null, errorCallback: ConstructorError = null) {
        val sessionId = preferenceHelper.getSessionId(sessionIncrementEventHandler)
        val sName = sectionName ?: preferenceHelper.defaultItemSection
        disposable.add(dataManager.trackSearchResultClick(itemName, customerId, searchTerm,
                arrayOf(Constants.QueryConstants.SESSION to sessionId.toString(),
                        Constants.QueryConstants.AUTOCOMPLETE_SECTION to sName)).subscribeOn(Schedulers.io())
                .subscribe({}, { t ->
                    t.printStackTrace()
                    errorCallback?.invoke(t)
                    e("Search result click event error: ${t.message}")
                }))
    }

    fun trackSearchResultsLoaded(term: String, resultCount: Int, errorCallback: ConstructorError = null) {
        val sessionId = preferenceHelper.getSessionId(sessionIncrementEventHandler)
        disposable.add(dataManager.trackSearchResultsLoaded(term, resultCount,
                arrayOf(Constants.QueryConstants.SESSION to sessionId.toString(),
                        Constants.QueryConstants.ACTION to Constants.QueryValues.EVENT_SEARCH_RESULTS)).subscribeOn(Schedulers.io())
                .subscribe({}, { t ->
                    t.printStackTrace()
                    errorCallback?.invoke(t)
                    e("Conversion event error: ${t.message}")
                }))
    }

    fun trackInputFocus(term: String?, errorCallback: ConstructorError = null) {
        val sessionId = preferenceHelper.getSessionId(sessionIncrementEventHandler)
        disposable.add(dataManager.trackInputFocus(term,
                arrayOf(Constants.QueryConstants.SESSION to sessionId.toString(),
                        Constants.QueryConstants.ACTION to Constants.QueryValues.EVENT_INPUT_FOCUS)).subscribeOn(Schedulers.io())
                .subscribe({}, { t ->
                    t.printStackTrace()
                    errorCallback?.invoke(t)
                    e("Input focus event error: ${t.message}")
                }))
    }

    fun trackPurchase(clientIds: Array<String>, sectionName: String? = null, errorCallback: ConstructorError = null) {
        val sessionId = preferenceHelper.getSessionId(sessionIncrementEventHandler)
        val sectionNameParam = sectionName ?: preferenceHelper.defaultItemSection
        val params = mutableListOf(Constants.QueryConstants.SESSION to sessionId.toString(),
                Constants.QueryConstants.AUTOCOMPLETE_SECTION to sectionNameParam)
        clientIds.forEach { params.add(Constants.QueryConstants.CUSTOMER_ID to it) }
        disposable.add(dataManager.trackPurchase(params.toTypedArray()).subscribeOn(Schedulers.io())
                .subscribe({}, { t ->
                    t.printStackTrace()
                    errorCallback?.invoke(t)
                    e("Input focus event error: ${t.message}")
                }))
    }

}