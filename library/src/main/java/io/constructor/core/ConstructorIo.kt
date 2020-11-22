package io.constructor.core

import android.annotation.SuppressLint
import android.content.Context
import io.constructor.data.ConstructorData
import io.constructor.data.DataManager
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.model.autocomplete.AutocompleteResponse
import io.constructor.data.model.common.Result
import io.constructor.data.model.common.ResultGroup
import io.constructor.data.model.search.SearchResponse
import io.constructor.data.model.browse.BrowseResponse
import io.constructor.injection.component.AppComponent
import io.constructor.injection.component.DaggerAppComponent
import io.constructor.injection.module.AppModule
import io.constructor.injection.module.NetworkModule
import io.constructor.util.broadcastIntent
import io.constructor.util.e
import io.constructor.util.urlEncode
import io.reactivex.Completable
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

    private var sessionIncrementHandler: (String) -> Unit = {
        trackSessionStart()
    }

    fun init(context: Context?, constructorIoConfig: ConstructorIoConfig) {
        if (context == null) {
            throw IllegalStateException("context is null, please init library using ConstructorIo.with(context)")
        }
        this.context = context.applicationContext

        configMemoryHolder = component.configMemoryHolder()
        configMemoryHolder.autocompleteResultCount = constructorIoConfig.autocompleteResultCount
        configMemoryHolder.testCellParams = constructorIoConfig.testCells

        preferenceHelper = component.preferenceHelper()
        preferenceHelper.apiKey = constructorIoConfig.apiKey
        preferenceHelper.serviceUrl = constructorIoConfig.serviceUrl
        preferenceHelper.port = constructorIoConfig.servicePort
        preferenceHelper.scheme = constructorIoConfig.serviceScheme
        preferenceHelper.defaultItemSection = constructorIoConfig.defaultItemSection
        if (preferenceHelper.id.isBlank()) {
            preferenceHelper.id = UUID.randomUUID().toString()
        }

        // Instantiate the data manager last (depends on the preferences helper)
        dataManager = component.dataManager()
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
    }

    fun appMovedToForeground() {
        preferenceHelper.getSessionId(sessionIncrementHandler)
    }

    /**
     * Returns a list of autocomplete suggestions
     */
    fun getAutocompleteResults(query: String): Observable<ConstructorData<AutocompleteResponse>> {
        val params = mutableListOf<Pair<String, String>>()
        configMemoryHolder.autocompleteResultCount?.entries?.forEach {
            params.add(Pair(Constants.QueryConstants.NUM_RESULTS+it.key, it.value.toString()))
        }
        return dataManager.getAutocompleteResults(query, params.toTypedArray())
    }

    /**
     * Returns search results including filters, categories, sort options, etc.
     */
    fun getSearchResults(text: String, facets: List<Pair<String, List<String>>>? = null, page: Int? = null, perPage: Int? = null, groupId: Int? = null, sortBy: String? = null, sortOrder: String? = null): Observable<ConstructorData<SearchResponse>> {
        val encodedParams: ArrayList<Pair<String, String>> = arrayListOf()
        groupId?.let { encodedParams.add(Constants.QueryConstants.FILTER_GROUP_ID.urlEncode() to it.toString()) }
        page?.let { encodedParams.add(Constants.QueryConstants.PAGE.urlEncode() to page.toString().urlEncode()) }
        perPage?.let { encodedParams.add(Constants.QueryConstants.PER_PAGE.urlEncode() to perPage.toString().urlEncode()) }
        sortBy?.let { encodedParams.add(Constants.QueryConstants.SORT_BY.urlEncode() to it.urlEncode()) }
        sortOrder?.let { encodedParams.add(Constants.QueryConstants.SORT_ORDER.urlEncode() to it.urlEncode()) }
        facets?.forEach { facet ->
            facet.second.forEach {
                encodedParams.add(Constants.QueryConstants.FILTER_FACET.format(facet.first).urlEncode() to it.urlEncode())
            }
        }
        return dataManager.getSearchResults(text, encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Tracks session start events
     */
    private fun trackSessionStart() {
        var completable = trackSessionStartInternal()
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Session Start event error: ${t.message}")
        }))
    }
    internal fun trackSessionStartInternal (): Completable {
        return dataManager.trackSessionStart(
                arrayOf(Constants.QueryConstants.ACTION to Constants.QueryValues.EVENT_SESSION_START)
        )
    }

    /**
     * Tracks input focus events
     */
    fun trackInputFocus(term: String?) {
        var completable = trackInputFocusInternal(term)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Input Focus event error: ${t.message}")
        }))
    }
    internal fun trackInputFocusInternal(term: String?): Completable {
        preferenceHelper.getSessionId(sessionIncrementHandler)
        return dataManager.trackInputFocus(term, arrayOf(
                Constants.QueryConstants.ACTION to Constants.QueryValues.EVENT_INPUT_FOCUS
        ));
    }

    /**
     * Tracks autocomplete select events
     */
    fun trackAutocompleteSelect(searchTerm: String, originalQuery: String, sectionName: String, resultGroup: ResultGroup? = null, resultID: String? = null) {
        var completable = trackAutocompleteSelectInternal(searchTerm, originalQuery, sectionName, resultGroup, resultID);
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({
            context.broadcastIntent(Constants.EVENT_QUERY_SENT, Constants.EXTRA_TERM to searchTerm)
        }, {
            t -> e("Autocomplete Select error: ${t.message}")
        }))
    }
    internal fun trackAutocompleteSelectInternal(searchTerm: String, originalQuery: String, sectionName: String, resultGroup: ResultGroup? = null, resultID: String? = null): Completable {
        preferenceHelper.getSessionId(sessionIncrementHandler)
        val encodedParams: ArrayList<Pair<String, String>> = arrayListOf()
        resultGroup?.groupId?.let { encodedParams.add(Constants.QueryConstants.GROUP_ID.urlEncode() to it) }
        resultGroup?.displayName?.let { encodedParams.add(Constants.QueryConstants.GROUP_DISPLAY_NAME.urlEncode() to it.urlEncode()) }
        resultID?.let { encodedParams.add(Constants.QueryConstants.RESULT_ID.urlEncode() to it.urlEncode()) }
        return dataManager.trackAutocompleteSelect(searchTerm, arrayOf(
            Constants.QueryConstants.AUTOCOMPLETE_SECTION to sectionName,
            Constants.QueryConstants.ORIGINAL_QUERY to originalQuery,
            Constants.QueryConstants.EVENT to Constants.QueryValues.EVENT_CLICK
        ), encodedParams.toTypedArray())
    }

    /**
     * Tracks search submit events
     */
    fun trackSearchSubmit(searchTerm: String, originalQuery: String, resultGroup: ResultGroup?) {
        var completable = trackSearchSubmitInternal(searchTerm, originalQuery, resultGroup)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({
            context.broadcastIntent(Constants.EVENT_QUERY_SENT, Constants.EXTRA_TERM to searchTerm)
        }, {
            t -> e("Search Submit error: ${t.message}")
        }))
    }
    internal fun trackSearchSubmitInternal(searchTerm: String, originalQuery: String, resultGroup: ResultGroup?): Completable {
        preferenceHelper.getSessionId(sessionIncrementHandler)
        val encodedParams: ArrayList<Pair<String, String>> = arrayListOf()
        resultGroup?.groupId?.let { encodedParams.add(Constants.QueryConstants.GROUP_ID.urlEncode() to it) }
        resultGroup?.displayName?.let { encodedParams.add(Constants.QueryConstants.GROUP_DISPLAY_NAME.urlEncode() to it.urlEncode()) }
        return dataManager.trackSearchSubmit(searchTerm, arrayOf(
                Constants.QueryConstants.ORIGINAL_QUERY to originalQuery,
                Constants.QueryConstants.EVENT to Constants.QueryValues.EVENT_SEARCH
        ), encodedParams.toTypedArray())
    }

    /**
     * Tracks search results loaded (a.k.a. search results viewed) events
     */
    fun trackSearchResultsLoaded(term: String, resultCount: Int) {
        var completable = trackSearchResultsLoadedInternal(term, resultCount)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Search Results Loaded error: ${t.message}")
        }))
    }
    internal fun trackSearchResultsLoadedInternal(term: String, resultCount: Int): Completable {
        preferenceHelper.getSessionId(sessionIncrementHandler)
        return dataManager.trackSearchResultsLoaded(term, resultCount, arrayOf(
                Constants.QueryConstants.ACTION to Constants.QueryValues.EVENT_SEARCH_RESULTS
        ))
    }

    /**
     * Tracks search result click events
     */
    fun trackSearchResultClick(itemName: String, customerId: String, searchTerm: String = Constants.QueryConstants.TERM_UNKNOWN, sectionName: String? = null, resultID: String? = null) {
        var completable = trackSearchResultClickInternal(itemName, customerId, searchTerm, sectionName, resultID)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Search Result Click error: ${t.message}")
        }))
    }
    internal fun trackSearchResultClickInternal(itemName: String, customerId: String, searchTerm: String = Constants.QueryConstants.TERM_UNKNOWN, sectionName: String? = null, resultID: String? = null): Completable {
        preferenceHelper.getSessionId(sessionIncrementHandler)
        val encodedParams: ArrayList<Pair<String, String>> = arrayListOf()
        resultID?.let { encodedParams.add(Constants.QueryConstants.RESULT_ID.urlEncode() to it.urlEncode()) }
        val sName = sectionName ?: preferenceHelper.defaultItemSection
        return dataManager.trackSearchResultClick(itemName, customerId, searchTerm, arrayOf(
                Constants.QueryConstants.AUTOCOMPLETE_SECTION to sName
        ), encodedParams.toTypedArray())

    }

    /**
     * Tracks conversion (a.k.a add to cart) events
     */
    fun trackConversion(itemName: String, customerId: String, revenue: Double?, searchTerm: String = Constants.QueryConstants.TERM_UNKNOWN, sectionName: String? = null) {
        var completable = trackConversionInternal(itemName, customerId, revenue, searchTerm, sectionName)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Conversion error: ${t.message}")
        }))
    }
    internal fun trackConversionInternal(itemName: String, customerId: String, revenue: Double?, searchTerm: String = Constants.QueryConstants.TERM_UNKNOWN, sectionName: String? = null): Completable {
        preferenceHelper.getSessionId(sessionIncrementHandler)
        val revenueString = revenue?.let { "%.2f".format(revenue) }
        return dataManager.trackConversion(searchTerm, itemName, customerId, revenueString, arrayOf(
                Constants.QueryConstants.AUTOCOMPLETE_SECTION to (sectionName ?: preferenceHelper.defaultItemSection)
        ))
    }

    /**
     * Tracks purchase events
     */
    fun trackPurchase(clientIds: Array<String>, revenue: Double?, orderID: String, sectionName: String? = null) {
        var completable = trackPurchaseInternal(clientIds, revenue, orderID, sectionName)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Purchase error: ${t.message}")
        }))
    }
    internal fun trackPurchaseInternal(clientIds: Array<String>, revenue: Double?, orderID: String, sectionName: String? = null): Completable {
        preferenceHelper.getSessionId(sessionIncrementHandler)
        val sectionNameParam = sectionName ?: preferenceHelper.defaultItemSection
        val revenueString = revenue?.let { "%.2f".format(revenue) }
        val params = mutableListOf(Constants.QueryConstants.AUTOCOMPLETE_SECTION to sectionNameParam)
        return dataManager.trackPurchase(clientIds.toList(), revenueString, orderID, params.toTypedArray())
    }

    /**
     * Returns browse results including filters, categories, sort options, etc.
     */
    fun getBrowseResults(filterName: String, filterValue: String, facets: List<Pair<String, List<String>>>? = null, page: Int? = null, perPage: Int? = null, groupId: Int? = null, sortBy: String? = null, sortOrder: String? = null): Observable<ConstructorData<BrowseResponse>> {
        val encodedParams: ArrayList<Pair<String, String>> = arrayListOf()
        groupId?.let { encodedParams.add(Constants.QueryConstants.FILTER_GROUP_ID.urlEncode() to it.toString()) }
        page?.let { encodedParams.add(Constants.QueryConstants.PAGE.urlEncode() to page.toString().urlEncode()) }
        perPage?.let { encodedParams.add(Constants.QueryConstants.PER_PAGE.urlEncode() to perPage.toString().urlEncode()) }
        sortBy?.let { encodedParams.add(Constants.QueryConstants.SORT_BY.urlEncode() to it.urlEncode()) }
        sortOrder?.let { encodedParams.add(Constants.QueryConstants.SORT_ORDER.urlEncode() to it.urlEncode()) }
        facets?.forEach { facet ->
            facet.second.forEach {
                encodedParams.add(Constants.QueryConstants.FILTER_FACET.format(facet.first).urlEncode() to it.urlEncode())
            }
        }
        return dataManager.getBrowseResults(filterName, filterValue, encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Tracks browse results loaded (a.k.a. browse results viewed) events
     */
    fun trackBrowseResultsLoaded(filterName: String, filterValue: String, resultCount: Int) {
        var completable = trackBrowseResultsLoadedInternal(filterName, filterValue, resultCount)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Browse Results Loaded error: ${t.message}")
        }))
    }
    internal fun trackBrowseResultsLoadedInternal(filterName: String, filterValue: String, resultCount: Int): Completable {
        preferenceHelper.getSessionId(sessionIncrementHandler)
        return dataManager.trackBrowseResultsLoaded(filterName, filterValue, resultCount, arrayOf(
                Constants.QueryConstants.ACTION to Constants.QueryValues.EVENT_BROWSE_RESULTS
        ))
    }

    /**
     * Tracks browse result click events
     */
    fun trackBrowseResultClick(filterName: String, filterValue: String, customerId: String, resultPositionOnPage: Int, sectionName: String? = null, resultID: String? = null) {
        var completable = trackBrowseResultClickInternal(filterName, filterValue, customerId, resultPositionOnPage, sectionName, resultID)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Browse Result Click error: ${t.message}")
        }))
    }
    internal fun trackBrowseResultClickInternal(filterName: String, filterValue: String, customerId: String, resultPositionOnPage: Int, sectionName: String? = null, resultID: String? = null): Completable {
        preferenceHelper.getSessionId(sessionIncrementHandler)
        val encodedParams: ArrayList<Pair<String, String>> = arrayListOf()
        resultID?.let { encodedParams.add(Constants.QueryConstants.RESULT_ID.urlEncode() to it.urlEncode()) }
        val sName = sectionName ?: preferenceHelper.defaultItemSection
        return dataManager.trackBrowseResultClick(filterName, filterValue, customerId, resultPositionOnPage, arrayOf(
                Constants.QueryConstants.AUTOCOMPLETE_SECTION to sName
        ), encodedParams.toTypedArray())

    }

}