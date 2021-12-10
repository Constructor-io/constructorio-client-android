package io.constructor.core

import android.annotation.SuppressLint
import android.content.Context
import io.constructor.BuildConfig
import io.constructor.data.ConstructorData
import io.constructor.data.DataManager
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.model.autocomplete.AutocompleteResponse
import io.constructor.data.model.common.ResultGroup
import io.constructor.data.model.search.SearchResponse
import io.constructor.data.model.browse.BrowseResponse
import io.constructor.data.model.recommendations.RecommendationsResponse
import io.constructor.data.model.browse.BrowseResultClickRequestBody
import io.constructor.data.model.browse.BrowseResultLoadRequestBody
import io.constructor.data.model.purchase.PurchaseItem
import io.constructor.data.model.purchase.PurchaseRequestBody
import io.constructor.data.model.conversion.ConversionRequestBody
import io.constructor.data.model.recommendations.RecommendationResultClickRequestBody
import io.constructor.data.model.recommendations.RecommendationResultViewRequestBody
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

/**
 * The Constructor SDK client used for getting results and tracking behavioural data.
 */
@SuppressLint("StaticFieldLeak")
object ConstructorIo {

    private lateinit var dataManager: DataManager
    private lateinit var preferenceHelper: PreferencesHelper
    private lateinit var configMemoryHolder: ConfigMemoryHolder
    private lateinit var context: Context
    private var disposable = CompositeDisposable()

    /**
     *  Sets the logged in user identifier
     */
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

    /**
     *  Initializes the client
     *  @param context The context
     *  @param constructorIoConfig The client configuration
     */
    fun init(context: Context?, constructorIoConfig: ConstructorIoConfig) {
        if (context == null) {
            throw IllegalStateException("context is null, please init library using ConstructorIo.with(context)")
        }
        this.context = context.applicationContext

        configMemoryHolder = component.configMemoryHolder()
        configMemoryHolder.testCellParams = constructorIoConfig.testCells
        configMemoryHolder.segments = constructorIoConfig.segments

        preferenceHelper = component.preferenceHelper()
        preferenceHelper.apiKey = constructorIoConfig.apiKey
        preferenceHelper.serviceUrl = constructorIoConfig.serviceUrl
        preferenceHelper.port = constructorIoConfig.servicePort
        preferenceHelper.scheme = constructorIoConfig.serviceScheme
        preferenceHelper.defaultItemSection = constructorIoConfig.defaultItemSection
        if (preferenceHelper.id!!.isBlank()) {
            preferenceHelper.id = UUID.randomUUID().toString()
        }

        // Instantiate the data manager last (depends on the preferences helper)
        dataManager = component.dataManager()
    }

    /**
     * Returns the current session identifier (an incrementing integer)
     */
    fun getSessionId() = preferenceHelper.getSessionId()

    /**
     * Returns the current client identifier (a random GUID assigned to the app running on the device)
     */
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

    /**
     * @suppress
     */
    fun appMovedToForeground() {
        preferenceHelper.getSessionId(sessionIncrementHandler)
    }

    /**
     * Returns a list of autocomplete suggestions
     * ##Example
     * ```
     * ConstructorIo.getAutocompleteResults("Dav", selectedFacet?.map { it.key to it.value })
     *      .subscribeOn(Schedulers.io())
     *      .observeOn(AndroidSchedulers.mainThread())
     *      .subscribe {
     *          it.onValue {
     *              it?.let {
     *                  view.renderData(it)
     *              }
     *          }
     *      }
     * ```
     * @param term The term to display results from
     * @param numResultsPerSection The number of results per section
     * @param facetsConfig The configuration of options related to facets [io.constructor.core.FacetsConfig]
     */
    fun getAutocompleteResults(term: String, numResultsPerSection: Map<String, Int>? = null, facetsConfig: FacetsConfig? = null): Observable<ConstructorData<AutocompleteResponse>> {
        val encodedParams: ArrayList<Pair<String, String>> = arrayListOf()

        numResultsPerSection?.entries?.forEach {
            encodedParams.add(Constants.QueryConstants.NUM_RESULTS+it.key to it.value.toString())
        }

        if (facetsConfig !== null) {
            facetsConfig.facets?.forEach { facet ->
                facet.value.forEach {
                    encodedParams.add(Constants.QueryConstants.FILTER_FACET.format(facet.key).urlEncode() to it.urlEncode())
                }
            }
            facetsConfig.hiddenFields?.forEach { hiddenField ->
                encodedParams.add(Constants.QueryConstants.HIDDEN_FIELD.urlEncode() to hiddenField.urlEncode())
            }
        }

        return dataManager.getAutocompleteResults(term, encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Returns a list of search results including filters, categories, sort options, etc.
     * ##Example
     * ```
     * ConstructorIo.getSearchResults("Dave's bread", ResultsConfig(2, 30), FacetsConfig(mapOf("Brands" to "Best Brand")))
     *      .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
     *      .subscribe {
     *          it.onValue {
     *              it.response?.let {
     *                  view.renderData(it)
     *              }
     *          }
     *      }
     * ```
     * @param term The term to display results from
     * @param resultsConfig The configuration of options related to displaying results [io.constructor.core.ResultsConfig]
     * @param facetsConfig The configuration of options related to facets [io.constructor.core.FacetsConfig]
     */
    fun getSearchResults(term: String, resultsConfig: ResultsConfig? = null, facetsConfig: FacetsConfig? = null): Observable<ConstructorData<SearchResponse>> {
        val encodedParams: ArrayList<Pair<String, String>> = arrayListOf()

        if (resultsConfig !== null) {
            resultsConfig.page?.let { encodedParams.add(Constants.QueryConstants.PAGE.urlEncode() to it.toString().urlEncode()) }
            resultsConfig.resultsPerPage?.let { encodedParams.add(Constants.QueryConstants.PER_PAGE.urlEncode() to it.toString().urlEncode()) }
            resultsConfig.sortBy?.let { encodedParams.add(Constants.QueryConstants.SORT_BY.urlEncode() to it.urlEncode()) }
            resultsConfig.sortOrder?.let { encodedParams.add(Constants.QueryConstants.SORT_ORDER.urlEncode() to it.urlEncode()) }
            resultsConfig.section?.let { encodedParams.add(Constants.QueryConstants.SECTION.urlEncode() to it.urlEncode()) }
        }

        if (facetsConfig !== null) {
            facetsConfig.facets?.forEach { facet ->
                facet.value.forEach {
                    encodedParams.add(Constants.QueryConstants.FILTER_FACET.format(facet.key).urlEncode() to it.urlEncode())
                }
            }
            facetsConfig.fmtOptions?.forEach { option ->
                    encodedParams.add(Constants.QueryConstants.FMT_OPTIONS.format(option.key).urlEncode() to option.value.urlEncode())
            }
            facetsConfig.hiddenFields?.forEach { hiddenField ->
                encodedParams.add(Constants.QueryConstants.HIDDEN_FIELD.urlEncode() to hiddenField.urlEncode())
            }
        }

        return dataManager.getSearchResults(term, encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Returns a list of browse results including filters, categories, sort options, etc.
     * ##Example
     * ```
     * ConstructorIo.getBrowseResults("group_id", "Beverages", ResultsConfig(2, 30), FacetsConfig(mapOf("Brands" to "Best Brand")))
     *      .subscribeOn(Schedulers.io())
     *      .observeOn(AndroidSchedulers.mainThread())
     *      .subscribe {
     *          it.onValue {
     *              it.response?.let {
     *                  view.renderData(it)
     *              }
     *          }
     *      }
     * ```
     * @param filterName The filter name to display results from
     * @param filterValue The filter value to display results from
     * @param resultsConfig The configuration of options related to displaying results [io.constructor.core.ResultsConfig]
     * @param facetsConfig The configuration of options related to facets [io.constructor.core.FacetsConfig]
     */
    fun getBrowseResults(filterName: String, filterValue: String, resultsConfig: ResultsConfig? = null, facetsConfig: FacetsConfig? = null): Observable<ConstructorData<BrowseResponse>> {
        val encodedParams: ArrayList<Pair<String, String>> = arrayListOf()

        if (resultsConfig !== null) {
            resultsConfig.page?.let { encodedParams.add(Constants.QueryConstants.PAGE.urlEncode() to it.toString().urlEncode()) }
            resultsConfig.resultsPerPage?.let { encodedParams.add(Constants.QueryConstants.PER_PAGE.urlEncode() to it.toString().urlEncode()) }
            resultsConfig.sortBy?.let { encodedParams.add(Constants.QueryConstants.SORT_BY.urlEncode() to it.urlEncode()) }
            resultsConfig.sortOrder?.let { encodedParams.add(Constants.QueryConstants.SORT_ORDER.urlEncode() to it.urlEncode()) }
            resultsConfig.section?.let { encodedParams.add(Constants.QueryConstants.SECTION.urlEncode() to it.urlEncode()) }
        }

        if (facetsConfig !== null) {
            facetsConfig.facets?.forEach { facet ->
                facet.value.forEach {
                    encodedParams.add(Constants.QueryConstants.FILTER_FACET.format(facet.key).urlEncode() to it.urlEncode())
                }
            }
            facetsConfig.fmtOptions?.forEach { option ->
                encodedParams.add(Constants.QueryConstants.FMT_OPTIONS.format(option.key).urlEncode() to option.value.urlEncode())
            }
            facetsConfig.hiddenFields?.forEach { hiddenField ->
                encodedParams.add(Constants.QueryConstants.HIDDEN_FIELD.urlEncode() to hiddenField.urlEncode())
            }
        }

        return dataManager.getBrowseResults(filterName, filterValue, encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Tracks session start events
     * ##Example
     * ```
     * ConstructorIo.trackSessionStart()
     * ```
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
     * ##Example
     * ```
     * ConstructorIo.trackInputFocus("food")
     * ```
     * @param term the term currently in the search bar
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
        ))
    }

    /**
     * Tracks autocomplete select events
     * ##Example
     * ```
     * ConstructorIo.trackAutocompleteSelect("toothpicks", "tooth", "Search Suggestions")
     * ```
     * @param searchTerm the term selected, i.e. "Pumpkin"
     * @param originalQuery the term in the search bar, i.e. "Pum"
     * @param sectionName the section the selection came from, i.e. "Search Suggestions"
     * @param resultGroup the group to search within if a user selected to search in a group, i.e. "Pumpkin in Canned Goods"
     * @param resultID the result ID of the autocomplete response that the selection came from
     */
    fun trackAutocompleteSelect(searchTerm: String, originalQuery: String, sectionName: String, resultGroup: ResultGroup? = null, resultID: String? = null) {
        var completable = trackAutocompleteSelectInternal(searchTerm, originalQuery, sectionName, resultGroup, resultID)
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
            Constants.QueryConstants.SECTION to sectionName,
            Constants.QueryConstants.ORIGINAL_QUERY to originalQuery,
            Constants.QueryConstants.EVENT to Constants.QueryValues.EVENT_CLICK
        ), encodedParams.toTypedArray())
    }

    /**
     * Tracks search submit events
     * ##Example
     * ```
     * ConstructorIo.trackSearchSubmit("toothpicks", "tooth")
     * ```
     * @param searchTerm the term selected, i.e. "Pumpkin"
     * @param originalQuery the term in the search bar, i.e. "Pum"
     * @param resultGroup the group to search within if a user elected to search in a group, i.e. "Pumpkin in Canned Goods"
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
     * ##Example
     * ```
     * ConstructorIo.trackSearchResultsLoaded("tooth", 789, arrayOf("1234567-AB", "1234567-AB"))
     * ```
     * @param term the term that results are displayed for, i.e. "Pumpkin"
     * @param resultCount the number of results for that term
     * @param customerIds the customerIds of shown items
     */
    fun trackSearchResultsLoaded(term: String, resultCount: Int, customerIds: Array<String>? = null) {
        var completable = trackSearchResultsLoadedInternal(term, resultCount, customerIds)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Search Results Loaded error: ${t.message}")
        }))
    }
    internal fun trackSearchResultsLoadedInternal(term: String, resultCount: Int, customerIds: Array<String>? = null): Completable {
        preferenceHelper.getSessionId(sessionIncrementHandler)
        return dataManager.trackSearchResultsLoaded(term, resultCount, customerIds, arrayOf(
                Constants.QueryConstants.ACTION to Constants.QueryValues.EVENT_SEARCH_RESULTS
        ))
    }

    /**
     * Tracks search result click events
     * ##Example
     * ```
     * ConstructorIo.trackSearchResultClick("Fashionable Toothpicks", "1234567-AB", "tooth", "Products", "179b8a0e-3799-4a31-be87-127b06871de2")
     * ```
     * @param itemName the name of the clicked item i.e. "Kabocha Pumpkin"
     * @param customerId the identifier of the clicked item i.e "PUMP-KAB-0002"
     * @param searchTerm the term that results are displayed for, i.e. "Pumpkin"
     * @param sectionName the section that the results came from, i.e. "Products"
     * @param resultID the result ID of the search response that the click came from
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
                Constants.QueryConstants.SECTION to sName
        ), encodedParams.toTypedArray())

    }

    /**
     * Tracks conversion (a.k.a add to cart) events
     *
     * ##Example
     * ```
     * ConstructorIo.trackConversion("Fashionable Toothpicks", "1234567-AB", 12.99, "tooth", "Products", "add_to_cart")
     * ```
     * @param itemName the name of the converting item i.e. "Kabocha Pumpkin"
     * @param customerId the identifier of the converting item i.e "PUMP-KAB-0002"
     * @param searchTerm the search term that lead to the event (if adding to cart in a search flow)
     * @param sectionName the section that the results came from, i.e. "Products"
     * @param conversionType the type of conversion, i.e. "add_to_cart"
     */
    fun trackConversion(itemName: String, customerId: String, revenue: Double?, searchTerm: String = Constants.QueryConstants.TERM_UNKNOWN, sectionName: String? = null, conversionType: String? = null) {
        var completable = trackConversionInternal(itemName, customerId, revenue, searchTerm, sectionName, conversionType)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Conversion error: ${t.message}")
        }))
    }
    internal fun trackConversionInternal(itemName: String, customerId: String, revenue: Double?, searchTerm: String = Constants.QueryConstants.TERM_UNKNOWN, sectionName: String? = null, conversionType: String? = null): Completable {
        preferenceHelper.getSessionId(sessionIncrementHandler)
        val section = sectionName ?: preferenceHelper.defaultItemSection
        val conversionRequestBody = ConversionRequestBody(
                searchTerm,
                customerId,
                itemName,
                String.format("%.2f", revenue),
                conversionType,
                BuildConfig.CLIENT_VERSION,
                preferenceHelper.id,
                preferenceHelper.getSessionId(),
                preferenceHelper.apiKey,
                configMemoryHolder.userId,
                configMemoryHolder.segments,
                true,
                section,
                System.currentTimeMillis()
        )

        return dataManager.trackConversion(conversionRequestBody, arrayOf())
    }

    /**
     * Tracks purchase events
     * ##Example
     * ```
     * ConstructorIo.trackPurchase(arrayOf("1234567-AB", "1234567-AB"), 25.98, "ORD-1312343")
     * ```
     * @param customerIds the identifiers of the purchased items
     * @param revenue the revenue of the purchase event
     * @param orderID the identifier of the order
    */
    fun trackPurchase(customerIds: Array<String>, revenue: Double?, orderID: String, sectionName: String? = null) {
        var completable = trackPurchaseInternal(customerIds, revenue, orderID, sectionName)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Purchase error: ${t.message}")
        }))
    }
    internal fun trackPurchaseInternal(customerIds: Array<String>, revenue: Double?, orderID: String, sectionName: String? = null): Completable {
        preferenceHelper.getSessionId(sessionIncrementHandler)
        val items = customerIds.map { item -> PurchaseItem(item) }
        val sectionNameParam = sectionName ?: preferenceHelper.defaultItemSection
        val params = mutableListOf(Constants.QueryConstants.SECTION to sectionNameParam)
        val purchaseRequestBody = PurchaseRequestBody(
                items.toList(),
                orderID,
                revenue,
                BuildConfig.CLIENT_VERSION,
                preferenceHelper.id,
                preferenceHelper.getSessionId(),
                configMemoryHolder.userId,
                configMemoryHolder.segments,
                preferenceHelper.apiKey,
                true,
                System.currentTimeMillis()
        )

        return dataManager.trackPurchase(purchaseRequestBody, params.toTypedArray())
    }

    /**
     * Tracks browse result loaded (a.k.a. browse results viewed) events
     * ##Example
     * ```
     * ConstructorIo.trackBrowseResultsLoaded("Category", "Snacks", 674)
     * ```
     * @param filterName the name of the primary filter, i.e. "Aisle"
     * @param filterValue the value of the primary filter, i.e. "Produce"
     * @param resultCount the number of results for that filter name/value pair
     */
    fun trackBrowseResultsLoaded(filterName: String, filterValue: String, resultCount: Int, sectionName: String? = null, url: String = "Not Available") {
        var completable = trackBrowseResultsLoadedInternal(filterName, filterValue, resultCount, sectionName, url)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Browse Results Loaded error: ${t.message}")
        }))
    }
    internal fun trackBrowseResultsLoadedInternal(filterName: String, filterValue: String, resultCount: Int, sectionName: String? = null, url: String = "Not Available"): Completable {
        preferenceHelper.getSessionId(sessionIncrementHandler)
        val section = sectionName ?: preferenceHelper.defaultItemSection
        val browseResultLoadRequestBody = BrowseResultLoadRequestBody(
                filterName,
                filterValue,
                resultCount,
                url,
                BuildConfig.CLIENT_VERSION,
                preferenceHelper.id,
                preferenceHelper.getSessionId(),
                preferenceHelper.apiKey,
                configMemoryHolder.userId,
                configMemoryHolder.segments,
                true,
                section,
                System.currentTimeMillis()
        )

        return dataManager.trackBrowseResultsLoaded(
                browseResultLoadRequestBody,
                arrayOf()
        )
    }

    /**
     * Tracks browse result click events
     * ##Example
     * ```
     * ConstructorIo.trackBrowseResultClick("Category", "Snacks", "7654321-BA", "4", "Products", "179b8a0e-3799-4a31-be87-127b06871de2")
     * ```
     * @param filterName the name of the primary filter, i.e. "Aisle"
     * @param filterValue the value of the primary filter, i.e. "Produce"
     * @param customerId the item identifier of the clicked item i.e "PUMP-KAB-0002"
     * @param resultPositionOnPage the position of the clicked item on the page i.e. 4
     * @param sectionName the section that the results came from, i.e. "Products"
     * @param resultID the result ID of the browse response that the selection came from
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
        val section = sectionName ?: preferenceHelper.defaultItemSection
        val browseResultClickRequestBody = BrowseResultClickRequestBody(
                filterName,
                filterValue,
                customerId,
                resultPositionOnPage,
                BuildConfig.CLIENT_VERSION,
                preferenceHelper.id,
                preferenceHelper.getSessionId(),
                preferenceHelper.apiKey,
                configMemoryHolder.userId,
                configMemoryHolder.segments,
                true,
                section,
                System.currentTimeMillis()
        )

        return dataManager.trackBrowseResultClick(
                browseResultClickRequestBody,
                arrayOf(Constants.QueryConstants.SECTION to section),
                encodedParams.toTypedArray()
        )

    }

    /**
     * Returns a list of search results including filters, categories, sort options, etc.
     * ##Example
     * ```
     * ConstructorIo.getRecommendationResults(podId, selectedFacets?.map { it.key to it.value }, numResults)
     *      .subscribeOn(Schedulers.io())
     *      .observeOn(AndroidSchedulers.mainThread())
     *      .subscribe {
     *          it.onValue {
     *              it.response?.let {
     *                  view.renderData(it)
     *              }
     *          }
     *      }
     * ```
     * @param podId the pod id
     * @param facets  additional facets used to refine results
     * @param numResults the number of results to return
     * @param sectionName the section the selection will come from, i.e. "Products"
     * @param itemId: The item id to retrieve recommendations (strategy specific)
     * @param term: The term to use to refine results (strategy specific)
     */
    fun getRecommendationResults(podId: String, facets: List<Pair<String, List<String>>>? = null, numResults: Int? = null, sectionName: String? = null, itemId: String? = null, term: String? = null): Observable<ConstructorData<RecommendationsResponse>> {
        val encodedParams: ArrayList<Pair<String, String>> = arrayListOf()
        numResults?.let { encodedParams.add(Constants.QueryConstants.NUM_RESULT.urlEncode() to numResults.toString().urlEncode()) }
        sectionName?.let { encodedParams.add(Constants.QueryConstants.SECTION.urlEncode() to sectionName.toString().urlEncode()) }
        itemId?.let { encodedParams.add(Constants.QueryConstants.ITEM_ID.urlEncode() to itemId.toString().urlEncode()) }
        term?.let { encodedParams.add(Constants.QueryConstants.TERM.urlEncode() to term.toString().urlEncode()) }
        facets?.forEach { facet ->
            facet.second.forEach {
                encodedParams.add(Constants.QueryConstants.FILTER_FACET.format(facet.first).urlEncode() to it.urlEncode())
            }
        }
        return dataManager.getRecommendationResults(podId, encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Tracks recommendation result click events
     * ##Example
     * ```
     * ConstructorIo.trackRecommendationResultClick("Best_Sellers", "User Featured", "7654321-BA", null, "Products", "179b8a0e-3799-4a31-be87-127b06871de2", 4, 1, 4, 2)
     * ```
     * @param podId The pod id
     * @param strategyId The strategy id
     * @param customerId The item identifier of the clicked item i.e "PUMP-KAB-0002"
     * @param variationId The variation item identifier of the clicked item
     * @param sectionName The section that the results came from, i.e. "Products"
     * @param resultId The result ID of the recommendation response that the selection came from
     * @param numResultsPerPage The count of recommendation results on each page
     * @param resultPage The current page that recommendation result is on
     * @param resultCount The total number of recommendation results
     * @param resultPositionOnPage The position of the recommendation result that was clicked on
     */
    fun trackRecommendationResultClick(podId: String, strategyId: String, customerId: String, variationId: String? = null, sectionName: String? = null, resultId: String? = null, numResultsPerPage: Int? = null, resultPage: Int? = null, resultCount: Int? = null, resultPositionOnPage: Int? = null) {
        var completable = trackRecommendationResultClickInternal(podId, strategyId, customerId, variationId, sectionName, resultId, numResultsPerPage, resultPage, resultCount, resultPositionOnPage)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Recommendation Result Click error: ${t.message}")
        }))
    }
    internal fun trackRecommendationResultClickInternal(podId: String, strategyId: String, customerId: String, variationId: String? = null, sectionName: String? = null, resultId: String? = null, numResultsPerPage: Int? = null, resultPage: Int? = null, resultCount: Int? = null, resultPositionOnPage: Int? = null): Completable {
        preferenceHelper.getSessionId(sessionIncrementHandler)
        val section = sectionName ?: preferenceHelper.defaultItemSection
        val recommendationsResultClickRequestBody = RecommendationResultClickRequestBody(
                podId,
                strategyId,
                customerId,
                variationId,
                resultId,
                numResultsPerPage,
                resultPage,
                resultCount,
                resultPositionOnPage,
                BuildConfig.CLIENT_VERSION,
                preferenceHelper.id,
                preferenceHelper.getSessionId(),
                preferenceHelper.apiKey,
                configMemoryHolder.userId,
                configMemoryHolder.segments,
                true,
                section,
                System.currentTimeMillis()
        )

        return dataManager.trackRecommendationResultClick(
                recommendationsResultClickRequestBody,
                arrayOf(Constants.QueryConstants.SECTION to section)
        )
    }

    /**
     * Tracks recommendation result view events
     * ##Example
     * ```
     * ConstructorIo.trackRecommendationResultsView("Best_Sellers", "User Featured", 4, 1, 4, "179b8a0e-3799-4a31-be87-127b06871de2", "Products")
     * ```
     * @param podId The pod id
     * @param numResultsViewed The count of recommendation results being viewed
     * @param resultPage The current page that recommendation result is on
     * @param resultCount The total number of recommendation results
     * @param resultId The result ID of the recommendation response that the selection came from
     * @param sectionName The section that the results came from, i.e. "Products"
     */
    fun trackRecommendationResultsView(podId: String, numResultsViewed: Int, resultPage: Int? = null, resultCount: Int? = null, resultId: String? = null, sectionName: String? = null, url: String = "Not Available") {
        var completable = trackRecommendationResultsViewInternal(podId, numResultsViewed, resultPage, resultCount, resultId, sectionName, url)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Recommendation Results View error: ${t.message}")
        }))
    }
    internal fun trackRecommendationResultsViewInternal(podId: String, numResultsViewed: Int, resultPage: Int? = null, resultCount: Int? = null, resultId: String? = null, sectionName: String? = null, url: String = "Not Available"): Completable {
        preferenceHelper.getSessionId(sessionIncrementHandler)
        val section = sectionName ?: preferenceHelper.defaultItemSection
        val recommendationResultViewRequestBody = RecommendationResultViewRequestBody(
                podId,
                numResultsViewed,
                resultPage,
                resultCount,
                resultId,
                url,
                BuildConfig.CLIENT_VERSION,
                preferenceHelper.id,
                preferenceHelper.getSessionId(),
                preferenceHelper.apiKey,
                configMemoryHolder.userId,
                configMemoryHolder.segments,
                true,
                section,
                System.currentTimeMillis()
        )

        return dataManager.trackRecommendationResultsView(
                recommendationResultViewRequestBody,
                arrayOf(Constants.QueryConstants.SECTION to section)
        )
    }
}
