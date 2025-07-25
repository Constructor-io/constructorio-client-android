package io.constructor.core

import android.annotation.SuppressLint
import android.content.Context
import com.squareup.moshi.Moshi
import io.constructor.BuildConfig
import io.constructor.data.ConstructorData
import io.constructor.data.DataManager
import io.constructor.data.builder.*
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.model.autocomplete.AutocompleteResponse
import io.constructor.data.model.browse.*
import io.constructor.data.model.common.ResultGroup
import io.constructor.data.model.common.TrackingItem
import io.constructor.data.model.common.VariationsMap
import io.constructor.data.model.conversion.ConversionRequestBody
import io.constructor.data.model.purchase.PurchaseItem
import io.constructor.data.model.purchase.PurchaseRequestBody
import io.constructor.data.model.quiz.*
import io.constructor.data.model.recommendations.RecommendationResultClickRequestBody
import io.constructor.data.model.recommendations.RecommendationResultViewRequestBody
import io.constructor.data.model.recommendations.RecommendationsResponse
import io.constructor.data.model.search.*
import io.constructor.data.model.tracking.GenericResultClickRequestBody
import io.constructor.data.model.tracking.ItemDetailLoadRequestBody
import io.constructor.injection.component.AppComponent
import io.constructor.injection.component.DaggerAppComponent
import io.constructor.injection.module.AppModule
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
    private val moshi = Moshi.Builder().build()
    private val jsonAdapter = moshi.adapter(VariationsMap::class.java)

    /**
     *  Sets the logged in user identifier
     */
    var userId: String?
        get() = configMemoryHolder.userId
        set(value) {
            configMemoryHolder.userId = value
        }

    /**
     *  Sets the test cells param
     */
    var testCells: List<Pair<String, String>?>
        get() = configMemoryHolder.testCellParams
        set(value) {
            configMemoryHolder.testCellParams = value
        }

    /**
     *  Sets the segments param
     */
    var segments: List<String?>
        get() = configMemoryHolder.segments
        set(value) {
            configMemoryHolder.segments = value
        }

    /**
     *  Sets the default analytics tags param
     */
    var defaultAnalyticsTags: Map<String, String>?
        get() = configMemoryHolder.defaultAnalyticsTags
        set(value) {
            configMemoryHolder.defaultAnalyticsTags = defaultAnalyticsTags
        }

    internal val component: AppComponent by lazy {
        DaggerAppComponent.builder()
                .appModule(AppModule(context))
                .build()
    }

    private var sessionIncrementHandler: (String) -> Unit = {
        trackSessionStart()
    }

    /**
     *  Initializes the client
     *
     *  @param context the context
     *  @param constructorIoConfig the client configuration
     */
    fun init(context: Context?, constructorIoConfig: ConstructorIoConfig) {
        if (context == null) {
            throw IllegalStateException("context is null, please init library using ConstructorIo.with(context)")
        }
        this.context = context.applicationContext

        configMemoryHolder = component.configMemoryHolder()
        configMemoryHolder.autocompleteResultCount = constructorIoConfig.autocompleteResultCount
        configMemoryHolder.testCellParams = constructorIoConfig.testCells
        configMemoryHolder.segments = constructorIoConfig.segments
        configMemoryHolder.defaultAnalyticsTags = constructorIoConfig.defaultAnalyticsTags

        preferenceHelper = component.preferenceHelper()
        preferenceHelper.apiKey = constructorIoConfig.apiKey
        preferenceHelper.serviceUrl = constructorIoConfig.serviceUrl
        preferenceHelper.quizzesServiceUrl = constructorIoConfig.quizzesServiceUrl
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
     *  Sets the sessionId
     */
    fun setSessionId(sessionId: Int) {
        preferenceHelper.setSessionId(sessionId);
    }

    /**
     * Returns the current client identifier (a random GUID assigned to the app running on the device)
     */
    fun getClientId() = preferenceHelper.id

    /**
     *  Sets the clientId
     */
    fun setClientId(clientId: String) {
        preferenceHelper.id = clientId;
    }

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

    private fun getEncodedParams(
        groupIdInt: Int? = null,
        groupId: String? = null,
        groupDisplayName: String? = null,
        page: Int? = null,
        offset: Int? = null,
        perPage: Int? = null,
        sortBy: String? = null,
        sortOrder: String? = null,
        sectionName: String? = null,
        hiddenFacets: List<String>? = null,
        groupsSortBy: String? = null,
        groupsSortOrder: String? = null,
        facets: List<Pair<String, List<String>>>? = null,
        hiddenFields: List<String>? = null,
        variationsMap: VariationsMap? = null,
        numResultsPerSection: Map<String, Int>? = null,
        resultId: String? = null,
        numResults: Int? = null,
        itemId: String? = null,
        term: String? = null,
        itemIds: List<String>? = null,
        ids: List<String>? = null,
        showHiddenFacets: Boolean? = null,
        groupsMaxDepth: Int? = null,
        groupIdFilter: String? = null,
        preFilterExpression: String? = null,
        sectionFacets: Map<String, List<Pair<String, List<String>>>>? = null,
        fmtOptions: Map<String, Any>? = null,
        slCampaignId: String? = null,
        slCampaignOwner: String? = null
    ): ArrayList<Pair<String, String>> {

        val encodedParams: ArrayList<Pair<String, String>> = arrayListOf()

        groupIdInt?.let { encodedParams.add(Constants.QueryConstants.FILTER_GROUP_ID.urlEncode() to it.toString()) }
        facets?.forEach { facet ->
            facet.second.forEach {
                encodedParams.add(Constants.QueryConstants.FILTER_FACET.format(facet.first).urlEncode() to it.urlEncode())
            }
        }
        hiddenFields?.forEach { hiddenField ->
            encodedParams.add(Constants.QueryConstants.FMT_OPTIONS.format(Constants.QueryConstants.HIDDEN_FIELD).urlEncode() to hiddenField.urlEncode())
        }
        variationsMap?.let {
            var filterBy: String? = null

            // Due to encoding issue, must remove the stringified filter_by field and add afterwards
            if (variationsMap.filterBy !== null) {
                filterBy = variationsMap.filterBy
                variationsMap.filterBy = null
            }

            var variationsMapJSONString = jsonAdapter.toJson(variationsMap).replace("groupBy", Constants.QueryConstants.GROUP_BY)
            if (filterBy !== null) {
                variationsMapJSONString = variationsMapJSONString.replace(Regex("\\}$"), ",\"${Constants.QueryConstants.FILTER_BY}\":$filterBy}")
            }

            encodedParams.add(Constants.QueryConstants.VARIATIONS_MAP.urlEncode() to variationsMapJSONString.urlEncode())
            variationsMap.filterBy = filterBy
        }
        numResultsPerSection?.forEach { section ->
            encodedParams.add(Pair(Constants.QueryConstants.NUM_RESULTS+section.key, section.value.toString()))
        }
        page?.let { encodedParams.add(Constants.QueryConstants.PAGE.urlEncode() to page.toString().urlEncode()) }
        offset?.let { encodedParams.add(Constants.QueryConstants.OFFSET.urlEncode() to offset.toString().urlEncode()) }
        perPage?.let { encodedParams.add(Constants.QueryConstants.PER_PAGE.urlEncode() to perPage.toString().urlEncode()) }
        sortBy?.let { encodedParams.add(Constants.QueryConstants.SORT_BY.urlEncode() to it.urlEncode()) }
        sortOrder?.let { encodedParams.add(Constants.QueryConstants.SORT_ORDER.urlEncode() to it.urlEncode()) }
        sectionName?.let { encodedParams.add(Constants.QueryConstants.SECTION.urlEncode() to sectionName.toString().urlEncode()) }
        hiddenFacets?.forEach { hiddenFacet ->
            encodedParams.add(Constants.QueryConstants.FMT_OPTIONS.format(Constants.QueryConstants.HIDDEN_FACET).urlEncode() to hiddenFacet.urlEncode())
        }
        fmtOptions?.forEach { (key, value) ->
            when (value) {
                is List<*> -> {
                    value.forEach{ fmtOptionsValue ->
                        if (fmtOptionsValue is String) {
                            encodedParams.add(Constants.QueryConstants.FMT_OPTIONS.format(key).urlEncode() to fmtOptionsValue.urlEncode())
                        } else if (fmtOptionsValue is Number || fmtOptionsValue is Boolean) {
                            encodedParams.add(Constants.QueryConstants.FMT_OPTIONS.format(key).urlEncode() to fmtOptionsValue.toString())
                        }
                    }
                }
                is String -> {
                    encodedParams.add(Constants.QueryConstants.FMT_OPTIONS.format(key).urlEncode() to value.urlEncode())
                }
                is Number, is Boolean -> {
                    encodedParams.add(Constants.QueryConstants.FMT_OPTIONS.format(key).urlEncode() to value.toString())
                }
            }
        }
        showHiddenFacets?.let { encodedParams.add(Constants.QueryConstants.FMT_OPTIONS.format(Constants.QueryConstants.SHOW_HIDDEN_FACETS).urlEncode() to showHiddenFacets.toString().urlEncode()) }
    slCampaignId?.let { encodedParams.add("sl_campaign_id".urlEncode() to it.urlEncode()) }
    slCampaignOwner?.let { encodedParams.add("sl_campaign_owner".urlEncode() to it.urlEncode()) }
        groupsSortBy?.let { encodedParams.add(Constants.QueryConstants.FMT_OPTIONS.format(Constants.QueryConstants.GROUPS_SORT_BY).urlEncode() to groupsSortBy.urlEncode()) }
        groupsSortOrder?.let { encodedParams.add(Constants.QueryConstants.FMT_OPTIONS.format(Constants.QueryConstants.GROUPS_SORT_ORDER).urlEncode() to groupsSortOrder.urlEncode()) }
        resultId?.let { encodedParams.add(Constants.QueryConstants.RESULT_ID.urlEncode() to it.urlEncode()) }
        groupId?.let { encodedParams.add(Constants.QueryConstants.GROUP_ID.urlEncode() to it) }
        groupIdFilter?.let { encodedParams.add(Constants.QueryConstants.FILTER_GROUP_ID.urlEncode() to it) }
        groupsMaxDepth?.let { encodedParams.add(Constants.QueryConstants.FMT_OPTIONS.format(Constants.QueryConstants.GROUPS_MAX_DEPTH).urlEncode() to it.toString().urlEncode()) }
        groupDisplayName?.let { encodedParams.add(Constants.QueryConstants.GROUP_DISPLAY_NAME.urlEncode() to it.urlEncode()) }
        numResults?.let { encodedParams.add(Constants.QueryConstants.NUM_RESULT.urlEncode() to numResults.toString().urlEncode()) }
        itemId?.let { encodedParams.add(Constants.QueryConstants.ITEM_ID.urlEncode() to itemId.toString().urlEncode()) }
        term?.let { encodedParams.add(Constants.QueryConstants.TERM.urlEncode() to term.toString().urlEncode()) }
        itemIds?.forEach { itemId ->
            encodedParams.add(Constants.QueryConstants.ITEM_ID.urlEncode() to itemId.urlEncode())
        }
        ids?.forEach { id ->
            encodedParams.add(Constants.QueryConstants.IDS.urlEncode() to id.urlEncode())
        }
        preFilterExpression?.let { encodedParams.add(Constants.QueryConstants.PRE_FILTER_EXPRESSION.urlEncode() to it.urlEncode())}

        sectionFacets?.toList()?.forEach { section ->
            section.second.forEach { facet ->
                facet.second.forEach {
                    encodedParams.add(Constants.QueryConstants.SECTION_FILTER_FACET.format(section.first, facet.first).urlEncode() to it.urlEncode())
                }
            }
        }

        return encodedParams
    }

    private fun mergeAnalyticsTags(defaultAnalyticsTags: Map<String, String>?, analyticsTags: Map<String, String>?): Map<String, String>? {
        if (analyticsTags.isNullOrEmpty()) {
            return defaultAnalyticsTags
        } else if (!defaultAnalyticsTags.isNullOrEmpty()) {
            return defaultAnalyticsTags + analyticsTags
        }

        return analyticsTags
    }

    /**
     * Returns a list of autocomplete suggestions.
     *
     * Example:
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
     * @param term the term to search for
     * @param facets additional facets used to refine results
     * @param groupId category facet used to refine results
     * @param hiddenFields show fields that are hidden by default
     * @param variationsMap specify which attributes within variations should be returned
     * @param sectionFacets specify sections for additional facets used to refine results
     */
    fun getAutocompleteResults(term: String, facets: List<Pair<String, List<String>>>? = null, groupId: Int? = null, hiddenFields: List<String>? = null, variationsMap: VariationsMap? = null, sectionFacets: Map<String, List<Pair<String, List<String>>>>? = null): Observable<ConstructorData<AutocompleteResponse>> {
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(groupIdInt = groupId, facets = facets, hiddenFields = hiddenFields, variationsMap = variationsMap, sectionFacets = sectionFacets)

        configMemoryHolder.autocompleteResultCount?.entries?.forEach {
            encodedParams.add(Pair(Constants.QueryConstants.NUM_RESULTS+it.key, it.value.toString()))
        }

        return dataManager.getAutocompleteResults(term.urlEncode(), encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Returns a list of autocomplete suggestions.
     * This function should be called within a coroutine scope.
     *
     * Example:
     * ```
     *  runBlocking {
     *      launch {
     *          try {
     *              val autocompleteResults = constructorIo.getAutocompleteResultsCRT("Dav")
     *              // Do something with autocompleteResults
     *          } catch (e: Exception) {
     *              println(e)
     *          }
     *      }
     *  }
     * ```
     * @param term the term to search for
     * @param facets additional facets used to refine results
     * @param groupId category facet used to refine results
     * @param hiddenFields show fields that are hidden by default
     * @param variationsMap specify which attributes within variations should be returned
     * @param sectionFacets specify sections for additional facets used to refine results
     */
    suspend fun getAutocompleteResultsCRT(term: String, facets: List<Pair<String, List<String>>>? = null, groupId: Int? = null, hiddenFields: List<String>? = null, variationsMap: VariationsMap? = null, sectionFacets: Map<String, List<Pair<String, List<String>>>>? = null): AutocompleteResponse {
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(groupIdInt = groupId, facets = facets, hiddenFields = hiddenFields, variationsMap = variationsMap, sectionFacets = sectionFacets)

        configMemoryHolder.autocompleteResultCount?.entries?.forEach {
            encodedParams.add(Pair(Constants.QueryConstants.NUM_RESULTS+it.key, it.value.toString()))
        }

        return dataManager.getAutocompleteResultsCRT(term.urlEncode(), encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Returns a list of autocomplete suggestions utilizing a request object.
     *
     * Example
     * ```
     * val filters = mapOf(
     *      "group_id" to listOf("G1234"),
     *      "Brand" to listOf("Cnstrc")
     *      "Color" to listOf("Red", "Blue")
     * )
     * val request = AutocompleteRequest.Builder("Dav")
     *      .setFilters(filters)
     *      .setHiddenFields(listOf("hidden_field_1", "hidden_field_2"))
     *      .build()
     *
     * ConstructorIo.getAutocompleteResults(request)
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
     * @param request the autocomplete request object
     */
    fun getAutocompleteResults(request: AutocompleteRequest): Observable<ConstructorData<AutocompleteResponse>> {
        var listSectionFilters: Map<String, List<Pair<String, List<String>>>>? = null

        if (request.sectionFilters !== null) {
            listSectionFilters = mapOf()

            request.sectionFilters.forEach {
                listSectionFilters.plus(it.key to it.value.toList())
            }
        }

        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(facets = request.filters?.toList(), hiddenFields = request.hiddenFields, variationsMap = request.variationsMap, numResultsPerSection = request.numResultsPerSection, sectionFacets = listSectionFilters)

        return dataManager.getAutocompleteResults(request.term.urlEncode(), encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Returns a list of search results including filters, categories, sort options, etc.
     *
     * Example:
     * ```
     * ConstructorIo.getSearchResults("Dave's bread", selectedFacets?.map { it.key to it.value }, 1, 24)
     *      .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
     *      .subscribe {
     *          it.onValue {
     *              it.response?.let {
     *                  view.renderData(it)
     *              }
     *          }
     *      }
     * ```
     * @param term the term to search for
     * @param facets  additional facets used to refine results
     * @param page the page number of the results
     * @param perPage The number of results per page to return
     * @param groupId category facet used to refine results
     * @param sortBy the sort method for results
     * @param sortOrder the sort order for results
     * @param sectionName the section the results will come from defaults to "Products"
     * @param hiddenFields show fields that are hidden by default
     * @param hiddenFacets show facets that are hidden by default
     * @param groupsSortBy the sort method for groups
     * @param groupsSortOrder the sort order for groups
     * @param variationsMap specify which attributes within variations should be returned
     * @param preFilterExpression faceting expression to scope results
     * @param fmtOptions additional format options to pass
     */
    fun getSearchResults(term: String, facets: List<Pair<String, List<String>>>? = null, page: Int? = null, perPage: Int? = null, groupId: Int? = null, sortBy: String? = null, sortOrder: String? = null, sectionName: String? = null, hiddenFields: List<String>? = null, hiddenFacets: List<String>? = null, groupsSortBy: String? = null, groupsSortOrder: String? = null, variationsMap: VariationsMap? = null, preFilterExpression: String? = null, fmtOptions: Map<String, Any>? = null): Observable<ConstructorData<SearchResponse>> {
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(facets = facets, page = page, perPage = perPage, groupIdInt = groupId, sortBy = sortBy, sortOrder = sortOrder, sectionName = sectionName, hiddenFields = hiddenFields, hiddenFacets = hiddenFacets, groupsSortBy = groupsSortBy, groupsSortOrder = groupsSortOrder, variationsMap = variationsMap, preFilterExpression = preFilterExpression, fmtOptions = fmtOptions)

        return dataManager.getSearchResults(term.urlEncode(), encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Returns a list of search results including filters, categories, sort options, etc.
     * This function should be called within a coroutine scope.
     *
     * Example:
     * ```
     *  runBlocking {
     *      launch {
     *          try {
     *              val searchResults = constructorIo.getSearchResultsCRT("Dav")
     *              // Do something with searchResults
     *          } catch (e: Exception) {
     *              println(e)
     *          }
     *      }
     *  }
     * ```
     * @param term the term to search for
     * @param facets  additional facets used to refine results
     * @param page the page number of the results
     * @param perPage The number of results per page to return
     * @param groupId category facet used to refine results
     * @param sortBy the sort method for results
     * @param sortOrder the sort order for results
     * @param sectionName the section the results will come from defaults to "Products"
     * @param hiddenFields show fields that are hidden by default
     * @param hiddenFacets show facets that are hidden by default
     * @param groupsSortBy the sort method for groups
     * @param groupsSortOrder the sort order for groups
     * @param variationsMap specify which attributes within variations should be returned
     * @param preFilterExpression faceting expression to scope results
     * @param fmtOptions additional format options to pass
     */
    suspend fun getSearchResultsCRT(term: String, facets: List<Pair<String, List<String>>>? = null, page: Int? = null, perPage: Int? = null, groupId: Int? = null, sortBy: String? = null, sortOrder: String? = null, sectionName: String? = null, hiddenFields: List<String>? = null, hiddenFacets: List<String>? = null, groupsSortBy: String? = null, groupsSortOrder: String? = null, variationsMap: VariationsMap? = null, preFilterExpression: String? = null, fmtOptions: Map<String, Any>? = null): SearchResponse {
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(facets = facets, page = page, perPage = perPage, groupIdInt = groupId, sortBy = sortBy, sortOrder = sortOrder, sectionName = sectionName, hiddenFields = hiddenFields, hiddenFacets = hiddenFacets, groupsSortBy = groupsSortBy, groupsSortOrder = groupsSortOrder, variationsMap = variationsMap, preFilterExpression = preFilterExpression, fmtOptions = fmtOptions)

        return dataManager.getSearchResultsCRT(term.urlEncode(), encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Returns a list of search results including filters, categories, sort options, etc. utilizing a request object.
     *
     * Example:
     * ```
     * val filters = mapOf(
     *      "group_id" to listOf("G1234"),
     *      "Brand" to listOf("Cnstrc")
     *      "Color" to listOf("Red", "Blue")
     * )
     * val request = SearchRequest.Builder("Dav")
     *      .setFilters(filters)
     *      .setHiddenFacets(listOf("hidden_facet_1", "hidden_facet_2"))
     *      .build()
     *
     * ConstructorIo.getSearchResults(request)
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
     * @param request the search request object
     */
    fun getSearchResults(request: SearchRequest): Observable<ConstructorData<SearchResponse>> {
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(facets = request.filters?.toList(), page = request.page, perPage = request.perPage, sortBy = request.sortBy, sortOrder = request.sortOrder, sectionName = request.section, hiddenFields = request.hiddenFields, hiddenFacets = request.hiddenFacets, groupsSortBy = request.groupsSortBy, groupsSortOrder = request.groupsSortOrder, variationsMap = request.variationsMap, preFilterExpression = request.preFilterExpression, fmtOptions = request.fmtOptions)

        return dataManager.getSearchResults(request.term.urlEncode(), encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Returns a list of browse results including filters, categories, sort options, etc.
     *
     * Example:
     * ```
     * ConstructorIo.getBrowseResults("group_id", "Beverages", selectedFacets?.map { it.key to it.value }, 1, perPage = 24)
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
     * @param filterName filter name to display results from
     * @param filterValue filter value to display results from
     * @param facets  additional facets used to refine results
     * @param page the page number of the results
     * @param perPage The number of results per page to return
     * @param groupId category facet used to refine results
     * @param sortBy the sort method for results
     * @param sortOrder the sort order for results
     * @param sectionName the section the results will come from defaults to "Products"
     * @param hiddenFields show fields that are hidden by default
     * @param hiddenFacets show facets that are hidden by default
     * @param groupsSortBy the sort method for groups
     * @param groupsSortOrder the sort order for groups
     * @param variationsMap specify which attributes within variations should be returned
     * @param preFilterExpression faceting expression to scope results
     * @param fmtOptions additional format options to pass
     */
    fun getBrowseResults(filterName: String, filterValue: String, facets: List<Pair<String, List<String>>>? = null, page: Int? = null, perPage: Int? = null, groupId: Int? = null, sortBy: String? = null, sortOrder: String? = null, sectionName: String? = null, hiddenFields: List<String>? = null, hiddenFacets: List<String>? = null, groupsSortBy: String? = null, groupsSortOrder: String? = null, variationsMap: VariationsMap? = null, preFilterExpression: String? = null, fmtOptions: Map<String, Any>? = null): Observable<ConstructorData<BrowseResponse>> {
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(facets = facets, page = page, perPage = perPage, groupIdInt = groupId, sortBy = sortBy, sortOrder = sortOrder, sectionName = sectionName, hiddenFields = hiddenFields, hiddenFacets = hiddenFacets, groupsSortBy = groupsSortBy, groupsSortOrder = groupsSortOrder, variationsMap = variationsMap, preFilterExpression = preFilterExpression, fmtOptions = fmtOptions)

        return dataManager.getBrowseResults(filterName, filterValue, encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Returns a list of browse results including filters, categories, sort options, etc. utilizing a request object.
     *
     * Example:
     * ```
     * val filters = mapOf(
     *      "group_id" to listOf("G1234"),
     *      "Brand" to listOf("Cnstrc")
     *      "Color" to listOf("Red", "Blue")
     * )
     * val request = BrowseRequest.Builder("group_id", "123")
     *      .setFilters(filters)
     *      .setHiddenFacets(listOf("hidden_facet_1", "hidden_facet_2"))
     *      .build()
     *
     * ConstructorIo.getBrowseResults(request)
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
     * @param request the browse request object
     */
    fun getBrowseResults(request: BrowseRequest): Observable<ConstructorData<BrowseResponse>> {
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(facets = request.filters?.toList(), page = request.page, perPage = request.perPage, sortBy = request.sortBy, sortOrder = request.sortOrder, sectionName = request.section, hiddenFields = request.hiddenFields, hiddenFacets = request.hiddenFacets, groupsSortBy = request.groupsSortBy, groupsSortOrder = request.groupsSortOrder, variationsMap = request.variationsMap, preFilterExpression = request.preFilterExpression, fmtOptions = request.fmtOptions)

        return dataManager.getBrowseResults(request.filterName, request.filterValue, encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Returns a list of browse results including filters, categories, sort options, etc.
     * This function should be called within a coroutine scope.
     *
     * Example:
     * ```
     *  runBlocking {
     *      launch {
     *          try {
     *              val browseResults = constructorIo.getBrowseResultsCRT("group_id", "888")
     *              // Do something with browseResults
     *          } catch (e: Exception) {
     *              println(e)
     *          }
     *      }
     *  }
     * ```
     * @param filterName filter name to display results from
     * @param filterValue filter value to display results from
     * @param facets  additional facets used to refine results
     * @param page the page number of the results
     * @param perPage The number of results per page to return
     * @param groupId category facet used to refine results
     * @param sortBy the sort method for results
     * @param sortOrder the sort order for results
     * @param sectionName the section the results will come from defaults to "Products"
     * @param hiddenFields show fields that are hidden by default
     * @param hiddenFacets show facets that are hidden by default
     * @param groupsSortBy the sort method for groups
     * @param groupsSortOrder the sort order for groups
     * @param variationsMap specify which attributes within variations should be returned
     * @param preFilterExpression faceting expression to scope results
     * @param fmtOptions additional format options to pass
     */
    suspend fun getBrowseResultsCRT(filterName: String, filterValue: String, facets: List<Pair<String, List<String>>>? = null, page: Int? = null, perPage: Int? = null, groupId: Int? = null, sortBy: String? = null, sortOrder: String? = null, sectionName: String? = null, hiddenFields: List<String>? = null, hiddenFacets: List<String>? = null, groupsSortBy: String? = null, groupsSortOrder: String? = null, variationsMap: VariationsMap? = null, preFilterExpression: String? = null, fmtOptions: Map<String, Any>? = null): BrowseResponse {
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(facets = facets, page = page, perPage = perPage, groupIdInt = groupId, sortBy = sortBy, sortOrder = sortOrder, sectionName = sectionName, hiddenFields = hiddenFields, hiddenFacets = hiddenFacets, groupsSortBy = groupsSortBy, groupsSortOrder = groupsSortOrder, variationsMap = variationsMap, preFilterExpression = preFilterExpression, fmtOptions = fmtOptions)

        return dataManager.getBrowseResultsCRT(filterName, filterValue, encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Returns a list of browse facet results.
     *
     * Example:
     * ```
     * ConstructorIo.getBrowseFacets(1, 20)
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
     * @param page the page number of the results (Can't be used with offset)
     * @param perPage the number of results per page to return
     * @param offset the number of results to skip from the start (Can't be used with page)
     * @param showHiddenFacets show fields that are hidden by default
     */
    fun getBrowseFacets(page: Int? = null, perPage: Int? = null, offset: Int? = null, showHiddenFacets: Boolean? = null): Observable<ConstructorData<BrowseFacetsResponse>> {
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(page = page, perPage = perPage, offset = offset, showHiddenFacets = showHiddenFacets)

        return dataManager.getBrowseFacets(encodedParams = encodedParams.toTypedArray())
    }


    /**
     * Returns a list of browse facet results utilizing a request object.
     *
     * Example:
     * ```
     * val request = BrowseFacetsRequest.Builder()
     *      .setPage(2)
     *      .setNumResultsPerPage(40)
     *      .build()
     *
     * ConstructorIo.getBrowseFacets(request)
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
     * @param request the search request object
     */
    fun getBrowseFacets(request: BrowseFacetsRequest): Observable<ConstructorData<BrowseFacetsResponse>> {
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(page = request.page, perPage = request.numResultsPerPage, offset = request.offset, showHiddenFacets = request.showHiddenFacets)

        return dataManager.getBrowseFacets(encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Returns a list of browse facets results.
     * This function should be called within a coroutine scope.
     *
     * Example:
     * ```
     *  runBlocking {
     *      launch {
     *          try {
     *              val browseFacetsResults = constructorIo.getBrowseFacetsCRT(1)
     *              // Do something with browseFacetsResults
     *          } catch (e: Exception) {
     *              println(e)
     *          }
     *      }
     *  }
     * ```
     * @param page the page number of the results (Can't be used with offset)
     * @param perPage the number of results per page to return
     * @param offset the number of results to skip from the start (Can't be used with page)
     * @param showHiddenFacets show fields that are hidden by default
     */
    suspend fun getBrowseFacetsCRT(page: Int? = null, perPage: Int? = null, offset: Int? = null, showHiddenFacets: Boolean? = null): BrowseFacetsResponse {
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(page = page, perPage = perPage, offset = offset, showHiddenFacets = showHiddenFacets)

        return dataManager.getBrowseFacetsCRT(encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Returns a list of browse facet options results.
     *
     * Example:
     * ```
     * ConstructorIo.getBrowseFacetOptions("Brand", false)
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
     * @param facetName name of the facet whose options to return
     * @param showHiddenFacets show fields that are hidden by default
     */
    fun getBrowseFacetOptions(facetName: String, showHiddenFacets: Boolean? = null): Observable<ConstructorData<BrowseFacetOptionsResponse>> {
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(showHiddenFacets = showHiddenFacets)
        encodedParams.add(Constants.QueryConstants.FACET_NAME to facetName.urlEncode())

        return dataManager.getBrowseFacetOptions(encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Returns a list of browse facet options utilizing a request object.
     *
     * Example:
     * ```
     * val request = BrowseFacetOptionsRequest.Builder("Brand")
     *      .setShowHiddenFacets(true)
     *      .build()
     *
     * ConstructorIo.getBrowseFacetOptions(request)
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
     * @param request the browse facet options request object
     */
    fun getBrowseFacetOptions(request: BrowseFacetOptionsRequest): Observable<ConstructorData<BrowseFacetOptionsResponse>> {
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(showHiddenFacets = request.showHiddenFacets)
        encodedParams.add(Constants.QueryConstants.FACET_NAME to request.facetName.urlEncode())

        return dataManager.getBrowseFacetOptions(encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Returns a list of browse facet options results.
     * This function should be called within a coroutine scope.
     *
     * Example:
     * ```
     *  runBlocking {
     *      launch {
     *          try {
     *              val browseFacetOptionsResults = constructorIo.getBrowseFacetOptionsCRT("Brand", false)
     *              // Do something with browseFacetOptionsResults
     *          } catch (e: Exception) {
     *              println(e)
     *          }
     *      }
     *  }
     * ```
     * @param facetName name of the facet whose options to return
     * @param showHiddenFacets show fields that are hidden by default
     */
    suspend fun getBrowseFacetOptionsCRT(facetName: String, showHiddenFacets: Boolean? = null): BrowseFacetOptionsResponse {
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(showHiddenFacets = showHiddenFacets)
        encodedParams.add(Constants.QueryConstants.FACET_NAME to facetName.urlEncode())

        return dataManager.getBrowseFacetOptionsCRT(encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Returns a list of browse groups results.
     *
     * Example:
     * ```
     * ConstructorIo.getBrowseGroups("Brand", 5)
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
     * @param groupId id of the specific group to be included in the response
     * @param groupsMaxDepth maximum depth of group hierarchy to be included in response
     */
    fun getBrowseGroups(groupId: String? = null, groupsMaxDepth: Int? = null): Observable<ConstructorData<BrowseGroupsResponse>> {
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(groupIdFilter = groupId, groupsMaxDepth = groupsMaxDepth)

        return dataManager.getBrowseGroups(encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Returns a list of browse groups results utilizing a request object.
     *
     * Example:
     * ```
     * val request = BrowseGroupsRequest.Builder()
     *      .setGroupId("Brand")
     *      .setGroupsMaxDepth(5)
     *      .build()
     *
     * ConstructorIo.getBrowseGroups(request)
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
     * @param request the browse groups request object
     */
    fun getBrowseGroups(request: BrowseGroupsRequest): Observable<ConstructorData<BrowseGroupsResponse>> {
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(groupIdFilter = request.groupId, groupsMaxDepth = request.groupsMaxDepth)

        return dataManager.getBrowseGroups(encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Returns a list of browse groups results.
     * This function should be called within a coroutine scope.
     *
     * Example:
     * ```
     *  runBlocking {
     *      launch {
     *          try {
     *              val browseGroupsResults = constructorIo.getBrowseGroupsCRT("Brand", 3)
     *              // Do something with browseGroupsResults
     *          } catch (e: Exception) {
     *              println(e)
     *          }
     *      }
     *  }
     * ```
     * @param groupId id of the specific group to be included in the response
     * @param groupsMaxDepth maximum depth of group hierarchy to be included in response
     */
    suspend fun getBrowseGroupsCRT(groupId: String? = null, groupsMaxDepth: Int? = null): BrowseGroupsResponse {
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(groupIdFilter = groupId, groupsMaxDepth = groupsMaxDepth)

        return dataManager.getBrowseGroupsCRT(encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Returns a list of browse results from a list of item IDs including filters, categories, sort options, etc.
     * This function should be called within a coroutine scope.
     *
     * Example
     * ```
     *  runBlocking {
     *      launch {
     *          try {
     *              val browseItemResults = constructorIo.getBrowseItemsResultsCRT("group_id", "888")
     *              // Do something with browseItemsResults
     *          } catch (e: Exception) {
     *              println(e)
     *          }
     *      }
     *  }
     * ```
     * @param itemIds the list of item ids to retrieve recommendations for (strategy specific)
     * @param facets additional facets used to refine results
     * @param page the page number of the results
     * @param perPage The number of results per page to return
     * @param groupId category facet used to refine results
     * @param sortBy the sort method for results
     * @param sortOrder the sort order for results
     * @param sectionName the section the results will come from defaults to "Products"
     * @param hiddenFields show fields that are hidden by default
     * @param hiddenFacets show facets that are hidden by default
     * @param groupsSortBy the sort method for groups
     * @param groupsSortOrder the sort order for groups
     * @param variationsMap specify which attributes within variations should be returned
     * @param preFilterExpression faceting expression to scope results
     * @param fmtOptions additional format options to pass
     */
    suspend fun getBrowseItemsResultsCRT(itemIds: List<String>, facets: List<Pair<String, List<String>>>? = null, page: Int? = null, perPage: Int? = null, groupId: Int? = null, sortBy: String? = null, sortOrder: String? = null, sectionName: String? = null, hiddenFields: List<String>? = null, hiddenFacets: List<String>? = null, groupsSortBy: String? = null, groupsSortOrder: String? = null, variationsMap: VariationsMap? = null, preFilterExpression: String? = null, fmtOptions: Map<String, Any>? = null): BrowseResponse {
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(facets = facets, page = page, perPage = perPage, groupIdInt = groupId, sortBy = sortBy, sortOrder = sortOrder, sectionName = sectionName, hiddenFields = hiddenFields, hiddenFacets = hiddenFacets, ids = itemIds, groupsSortBy = groupsSortBy, groupsSortOrder = groupsSortOrder, variationsMap = variationsMap, preFilterExpression = preFilterExpression, fmtOptions = fmtOptions)

        return dataManager.getBrowseItemsResultsCRT(encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Returns a list of browse results from a list of item IDs including filters, categories, sort options, etc. utilizing a request object.
     *
     * Example:
     * ```
     * val filters = mapOf(
     *      "group_id" to listOf("G1234"),
     *      "Brand" to listOf("Cnstrc")
     *      "Color" to listOf("Red", "Blue")
     * )
     * val request = BrowseItemsRequest.Builder("group_id", "123")
     *      .setFilters(filters)
     *      .setHiddenFacets(listOf("hidden_facet_1", "hidden_facet_2"))
     *      .build()
     *
     * ConstructorIo.getBrowseItemsResults(request)
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
     * @param request the browse request object
     */
    fun getBrowseItemsResults(request: BrowseItemsRequest): Observable<ConstructorData<BrowseResponse>> {
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(facets = request.filters?.toList(), page = request.page, perPage = request.perPage, sortBy = request.sortBy, sortOrder = request.sortOrder, sectionName = request.section, hiddenFields = request.hiddenFields, hiddenFacets = request.hiddenFacets, groupsSortBy = request.groupsSortBy, groupsSortOrder = request.groupsSortOrder, variationsMap = request.variationsMap, ids = request.ids, preFilterExpression = request.preFilterExpression, fmtOptions = request.fmtOptions)

        return dataManager.getBrowseItemsResults(encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Returns the next quiz question including options and attributes.
     *
     * Example:
     * ```
     * val answers = listOf(
     *      listOf("1"),
     *      listOf("1", "2"),
     *      listOf("true"),
     *      listOf("seen"),
     * )
     * ConstructorIo.getQuizNextQuestion("quiz-id", answers, "version-id")
     * ```
     * @param quizId id of the quiz you want to retrieve
     * @param answers list of answers to send
     * @param quizVersionId version identifier for the quiz. version ID will be returned with the first request and it should be passed with subsequent requests. more information can be found: [https://docs.constructor.com/reference/configuration-quizzes](https://docs.constructor.com/reference/configuration-quizzes)
     * @param quizSessionId session identifier for the quiz. session ID will be returned with the first request and it should be passed with subsequent requests. more information can be found: [https://docs.constructor.com/reference/configuration-quizzes](https://docs.constructor.com/reference/configuration-quizzes)
     * @param sectionName the section the quiz and results come from. defaults to "Products"
     */
    fun getQuizNextQuestion(quizId: String, answers: List<List<String>>? = null, quizVersionId: String? = null, quizSessionId: String? = null, sectionName: String? = null): Observable<ConstructorData<QuizQuestionResponse>> {
        val encodedParams: ArrayList<Pair<String, String>> = arrayListOf()

        answers?.forEach { answer ->
            encodedParams.add(Constants.QueryConstants.ANSWERS.urlEncode() to answer.joinToString(",").urlEncode())
        }
        quizVersionId?.let { encodedParams.add(Constants.QueryConstants.QUIZ_VERSION_ID.urlEncode() to it.urlEncode()) }
        quizSessionId?.let { encodedParams.add(Constants.QueryConstants.QUIZ_SESSION_ID.urlEncode() to it.urlEncode()) }
        sectionName?.let { encodedParams.add(Constants.QueryConstants.SECTION.urlEncode() to it.urlEncode()) }

        return dataManager.getQuizNextQuestion(quizId, encodedParams.toTypedArray(), preferenceHelper)
    }

    /**
     * Returns the next quiz question including options and attributes utilizing a request object.
     *
     * Example:
     * ```
     * val answers = listOf(
     *      listOf("1"),
     *      listOf("1", "2"),
     *      listOf("true"),
     *      listOf("seen"),
     * )
     * val request = QuizRequest.Builder("quiz-id")
     *     .setAnswers(answers)
     *     .setVersionId("version-id")
     *     .build()
     * ConstructorIo.getQuizNextQuestion(request)
     * ```
     * @param request the quiz request object
     */
    fun getQuizNextQuestion(request: QuizRequest): Observable<ConstructorData<QuizQuestionResponse>> {
        val encodedParams: ArrayList<Pair<String, String>> = arrayListOf()

        request.answers?.forEach { answer ->
            encodedParams.add(Constants.QueryConstants.ANSWERS.urlEncode() to answer.joinToString(",").urlEncode())
        }
        request.quizVersionId?.let { encodedParams.add(Constants.QueryConstants.QUIZ_VERSION_ID.urlEncode() to it.urlEncode()) }
        request.quizSessionId?.let { encodedParams.add(Constants.QueryConstants.QUIZ_SESSION_ID.urlEncode() to it.urlEncode()) }
        request.section?.let { encodedParams.add(Constants.QueryConstants.SECTION.urlEncode() to it.urlEncode()) }

        return dataManager.getQuizNextQuestion(request.quizId, encodedParams.toTypedArray(), preferenceHelper)
    }

    /**
     * Return the next quiz question including options and attributes.
     * This function should be called within a coroutine scope.
     *
     * Example:
     * ```
     * runBlocking {
     *      launch {
     *          try {
     *              val answers = listOf(
     *                  listOf("1"),
     *                  listOf("1", "2"),
     *                  listOf("true"),
     *                  listOf("seen"),
     *              )
     *              val quizResults = constructorIo.getQuizNextQuestionCRT("quiz-id", answers, "version-id")
     *              // Do something with quizResults
     *          } catch (e: Exception) {
     *              println(e)
     *          }
     *      }
     *  }
     * ```
     */
    suspend fun getQuizNextQuestionCRT(quizId: String, answers: List<List<String>>? = null, quizVersionId: String? = null, quizSessionId: String? = null, sectionName: String? = null): QuizQuestionResponse {
        val encodedParams: ArrayList<Pair<String, String>> = arrayListOf()

        answers?.forEach { answer ->
            encodedParams.add(Constants.QueryConstants.ANSWERS.urlEncode() to answer.joinToString(",").urlEncode())
        }
        quizVersionId?.let { encodedParams.add(Constants.QueryConstants.QUIZ_VERSION_ID.urlEncode() to it.urlEncode()) }
        quizSessionId?.let { encodedParams.add(Constants.QueryConstants.QUIZ_SESSION_ID.urlEncode() to it.urlEncode()) }
        sectionName?.let { encodedParams.add(Constants.QueryConstants.SECTION.urlEncode() to it.urlEncode()) }

        return dataManager.getQuizNextQuestionCRT(quizId, encodedParams.toTypedArray(), preferenceHelper)
    }

    /**
     * Return a list of quiz results.
     *
     * Example:
     * ```
     * val answers = listOf(
     *      listOf("1"),
     *      listOf("1", "2"),
     *      listOf("true"),
     *      listOf("seen"),
     * )
     * ConstructorIo.getQuizResults("quiz-id", answers, "version-id")
     * ```
     * @param quizId id of the quiz you want to retrieve
     * @param answers list of answers to send
     * @param quizVersionId version identifier for the quiz. version ID will be returned with the first request and it should be passed with subsequent requests. more information can be found: [https://docs.constructor.com/reference/configuration-quizzes](https://docs.constructor.com/reference/configuration-quizzes)
     * @param quizSessionId session identifier for the quiz. session ID will be returned with the first request and it should be passed with subsequent requests. more information can be found: [https://docs.constructor.com/reference/configuration-quizzes](https://docs.constructor.com/reference/configuration-quizzes)
     * @param sectionName the section the quiz and results come from. defaults to "Products"
     * @param filters additional filters used to refine results
     * @param page the page number of the results
     * @param perPage The number of results per page to return
     */
    fun getQuizResults(quizId: String, answers: List<List<String>>? = null, quizVersionId: String? = null, quizSessionId: String? = null, sectionName: String? = null, filters: Map<String, List<String>>? = null, page: Int? = null, perPage: Int? = null): Observable<ConstructorData<QuizResultsResponse>> {
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(
            facets = filters?.toList(),
            page = page,
            perPage = perPage,
            sectionName = sectionName
        )

        answers?.forEach { answer ->
            encodedParams.add(Constants.QueryConstants.ANSWERS.urlEncode() to answer.joinToString(",").urlEncode())
        }
        quizVersionId?.let { encodedParams.add(Constants.QueryConstants.QUIZ_VERSION_ID.urlEncode() to it.urlEncode()) }
        quizSessionId?.let { encodedParams.add(Constants.QueryConstants.QUIZ_SESSION_ID.urlEncode() to it.urlEncode()) }

        return dataManager.getQuizResults(quizId, encodedParams.toTypedArray(), preferenceHelper)
    }

    /**
     * Return a list of quiz results utilizing a request object.
     *
     * Example:
     * ```
     * val answers = listOf(
     *      listOf("1"),
     *      listOf("1", "2"),
     *      listOf("true"),
     *      listOf("seen"),
     * )
     * val request = QuizRequest.Builder("quiz-id")
     *     .setAnswers(answers)
     *     .setVersionId("version-id")
     *     .build()
     * ConstructorIo.getQuizResults(request)
     * ```
     * @param request the quiz request object
     */
    fun getQuizResults(request: QuizRequest): Observable<ConstructorData<QuizResultsResponse>> {
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(
            facets = request.filters?.toList(),
            page = request.page,
            perPage = request.perPage,
            sectionName = request.section
        )

        request.answers?.forEach { answer ->
            encodedParams.add(Constants.QueryConstants.ANSWERS.urlEncode() to answer.joinToString(",").urlEncode())
        }
        request.quizVersionId?.let { encodedParams.add(Constants.QueryConstants.QUIZ_VERSION_ID.urlEncode() to it.urlEncode()) }
        request.quizSessionId?.let { encodedParams.add(Constants.QueryConstants.QUIZ_SESSION_ID.urlEncode() to it.urlEncode()) }

        return dataManager.getQuizResults(request.quizId, encodedParams.toTypedArray(), preferenceHelper)
    }

    /**
     * Return a list of quiz results.
     * This function should be called within a coroutine scope.
     *
     * Example:
     * ```
     * runBlocking {
     *      launch {
     *          try {
     *              val answers = listOf(
     *                  listOf("1"),
     *                  listOf("1", "2"),
     *                  listOf("true"),
     *                  listOf("seen"),
     *              )
     *              val quizResults = constructorIo.getQuizResultsCRT("quiz-id", answers, "version-id")
     *              // Do something with quizResults
     *          } catch (e: Exception) {
     *              println(e)
     *          }
     *      }
     *  }
     * ```
     */
    suspend fun getQuizResultsCRT(quizId: String, answers: List<List<String>>? = null, quizVersionId: String? = null, quizSessionId: String? = null, sectionName: String? = null, filters: Map<String, List<String>>? = null, page: Int? = null, perPage: Int? = null): QuizResultsResponse {
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(
            facets = filters?.toList(),
            page = page,
            perPage = perPage,
            sectionName = sectionName
        )

        answers?.forEach { answer ->
            encodedParams.add(Constants.QueryConstants.ANSWERS.urlEncode() to answer.joinToString(",").urlEncode())
        }
        quizVersionId?.let { encodedParams.add(Constants.QueryConstants.QUIZ_VERSION_ID.urlEncode() to it.urlEncode()) }
        quizSessionId?.let { encodedParams.add(Constants.QueryConstants.QUIZ_SESSION_ID.urlEncode() to it.urlEncode()) }

        return dataManager.getQuizResultsCRT(quizId, encodedParams.toTypedArray(), preferenceHelper)
    }

    /**
     * Tracks quiz result click events
     * ##Example
     * ```
     * ConstructorIo.trackQuizResultClick("coffee-quiz", "23AECMA-1EFKCI", "34NCUIEI-214CDN", "shirt-a", "shirt-a--reg", "White shirt", null, null, 10, 1, 10, null);
     * ```
     * @param quizId The quiz id
     * @param quizVersionId version identifier for the quiz. version ID will be returned in quiz request responses
     * @param quizSessionId session identifier for the quiz. session ID will be returned in quiz request responses
     * @param customerId The item identifier of the clicked item i.e "PUMP-KAB-0002"
     * @param variationId The variation item identifier of the clicked item
     * @param itemName The item name of the clicked item i.e "Jacket Denim"
     * @param sectionName The section that the results came from, i.e. "Products"
     * @param resultId The result ID of the quiz response that the selection came from
     * @param resultPositionOnPage the position of the clicked item on the page i.e. 4
     * @param numResultsPerPage The count of quiz results on each page
     * @param resultPage The current page that quiz result is on
     * @param resultCount The total number of quiz results
     * @param analyticsTags Additional analytics tags to pass
     */
    fun trackQuizResultClick(quizId: String, quizVersionId: String, quizSessionId: String, customerId: String, variationId: String? = null, itemName: String? = null, sectionName: String? = null, resultId: String? = null,resultPositionOnPage: Int? = null, numResultsPerPage: Int? = null, resultPage: Int? = null, resultCount: Int? = null, analyticsTags: Map<String, String>? = null) {
        var completable = trackQuizResultClickInternal(quizId, quizVersionId, quizSessionId, customerId, variationId, itemName, sectionName, resultId, resultPositionOnPage, numResultsPerPage, resultPage, resultCount, analyticsTags)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
                t -> e("Quiz Result Click error: ${t.message}")
        }))
    }
    internal fun trackQuizResultClickInternal(quizId: String, quizVersionId: String, quizSessionId: String, customerId: String, variationId: String? = null, itemName: String? = null, sectionName: String? = null, resultId: String? = null, resultPositionOnPage: Int? = null, numResultsPerPage: Int? = null, resultPage: Int? = null, resultCount: Int? = null, analyticsTags: Map<String, String>? = null): Completable {
        preferenceHelper.getSessionId(sessionIncrementHandler)
        val section = sectionName ?: preferenceHelper.defaultItemSection
        val quizResultClickRequestBody = QuizResultClickRequestBody(
            quizId,
            quizVersionId,
            quizSessionId,
            resultPage,
            resultCount,
            numResultsPerPage,
            resultPositionOnPage,
            resultId,
            customerId,
            itemName,
            variationId,
            BuildConfig.CLIENT_VERSION,
            preferenceHelper.id,
            preferenceHelper.getSessionId(),
            preferenceHelper.apiKey,
            configMemoryHolder.userId,
            configMemoryHolder.segments,
            mergeAnalyticsTags(configMemoryHolder.defaultAnalyticsTags, analyticsTags),
    true,
            section,
            System.currentTimeMillis()
        )

        return dataManager.trackQuizResultClick(
            quizResultClickRequestBody,
            arrayOf(Constants.QueryConstants.SECTION to section)
        )
    }

    /**
     * Tracks quiz result load events
     * ##Example
     * ```
     * ConstructorIo.trackQuizResultLoad("coffee-quiz", "23AECMA-1EFKCI", "34NCUIEI-214CDN", null, null, 1, 10, null)
     * ```
     * @param quizId The quiz id
     * @param quizVersionId version identifier for the quiz. version ID will be returned in quiz request responses
     * @param quizSessionId session identifier for the quiz. session ID will be returned in quiz request responses
     * @param sectionName The section that the results came from, i.e. "Products"
     * @param resultId The result ID of the quiz response that the selection came from
     * @param resultPage The current page that quiz result is on
     * @param resultCount The total number of quiz results
     * @param analyticsTags Additional analytics tags to pass
     */
    fun trackQuizResultLoad(quizId: String, quizVersionId: String, quizSessionId: String, sectionName: String? = null, resultId: String? = null, resultPage: Int? = null, resultCount: Int? = null, url: String = "Not Available", analyticsTags: Map<String, String>? = null) {
        var completable = trackQuizResultLoadInternal(quizId, quizVersionId, quizSessionId, sectionName, resultId, resultPage, resultCount, url, analyticsTags)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
                t -> e("Quiz Result Load error: ${t.message}")
        }))
    }
    internal fun trackQuizResultLoadInternal(quizId: String, quizVersionId: String, quizSessionId: String, sectionName: String? = null, resultId: String? = null, resultPage: Int? = null, resultCount: Int? = null, url: String = "Not Available", analyticsTags: Map<String, String>? = null): Completable {
        preferenceHelper.getSessionId(sessionIncrementHandler)
        val section = sectionName ?: preferenceHelper.defaultItemSection
        val quizResultLoadRequestBody = QuizResultLoadRequestBody(
            quizId,
            quizVersionId,
            quizSessionId,
            url,
            resultPage,
            resultCount,
            resultId,
            BuildConfig.CLIENT_VERSION,
            preferenceHelper.id,
            preferenceHelper.getSessionId(),
            preferenceHelper.apiKey,
            configMemoryHolder.userId,
            configMemoryHolder.segments,
            mergeAnalyticsTags(configMemoryHolder.defaultAnalyticsTags, analyticsTags),
    true,
            section,
            System.currentTimeMillis()
        )

        return dataManager.trackQuizResultLoad(
            quizResultLoadRequestBody,
            arrayOf(Constants.QueryConstants.SECTION to section)
        )
    }

    /**
     * Tracks quiz conversion events
     * ##Example
     * ```
     * ConstructorIo.trackQuizConversion("coffee-quiz", "23AECMA-1EFKCI", "34NCUIEI-214CDN", null, null, null, "shirt-a", "shirt-a--reg", "White shirt", null, "129.99", null)
     * ```
     * @param quizId The quiz id
     * @param quizVersionId Version identifier for the quiz. version ID will be returned in quiz request responses
     * @param quizSessionId Session identifier for the quiz. session ID will be returned in quiz request responses
     * @param displayName Display name for the conversion event. This display name will be shown on the dashboard in the future. Required if isCustomType is true.
     * @param type Type of conversion event. Defaults to add_to_cart if not specified
     * @param isCustomType Specifies if the conversion type is a custom event
     * @param customerId The item identifier of the clicked item i.e "PUMP-KAB-0002"
     * @param variationId The variation item identifier of the clicked item
     * @param itemName The item name of the clicked item i.e "Jacket Denim"
     * @param sectionName The section that the results came from, i.e. "Products"
     * @param revenue The revenue of the item converted
     * @param analyticsTags Additional analytics tags to pass
     */
    fun trackQuizConversion(quizId: String, quizVersionId: String, quizSessionId: String, displayName: String? = null, type: String? = null, isCustomType: Boolean? = null, customerId: String, variationId: String? = null, itemName: String? = null, sectionName: String? = null, revenue: String? = null, analyticsTags: Map<String, String>? = null) {
        var completable = trackQuizConversionInternal(quizId, quizVersionId, quizSessionId, displayName, type, isCustomType, customerId, variationId, itemName, sectionName, revenue, analyticsTags)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
                t -> e("Quiz Conversion error: ${t.message}")
        }))
    }
    internal fun trackQuizConversionInternal(quizId: String, quizVersionId: String, quizSessionId: String, displayName: String? = null, type: String? = null, isCustomType: Boolean? = null, customerId: String, variationId: String? = null, itemName: String? = null, sectionName: String? = null, revenue: String? = null, analyticsTags: Map<String, String>? = null): Completable {
        preferenceHelper.getSessionId(sessionIncrementHandler)
        val section = sectionName ?: preferenceHelper.defaultItemSection
        val quizConversionRequestBody = QuizConversionRequestBody(
            quizId,
            quizVersionId,
            quizSessionId,
            displayName,
            type,
            isCustomType,
            customerId,
            itemName,
            variationId,
            revenue,
            BuildConfig.CLIENT_VERSION,
            preferenceHelper.id,
            preferenceHelper.getSessionId(),
            preferenceHelper.apiKey,
            configMemoryHolder.userId,
            configMemoryHolder.segments,
            mergeAnalyticsTags(configMemoryHolder.defaultAnalyticsTags, analyticsTags),
    true,
            section,
            System.currentTimeMillis()
        )

        return dataManager.trackQuizConversion(
            quizConversionRequestBody,
            arrayOf(Constants.QueryConstants.SECTION to section)
        )
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
     * Tracks input focus events.
     *
     * Example:
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
     * Tracks autocomplete select events.
     *
     * Example:
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
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(groupId = resultGroup?.groupId, groupDisplayName = resultGroup?.displayName, resultId = resultID)

        return dataManager.trackAutocompleteSelect(searchTerm, arrayOf(
            Constants.QueryConstants.SECTION to sectionName,
            Constants.QueryConstants.ORIGINAL_QUERY to originalQuery,
            Constants.QueryConstants.EVENT to Constants.QueryValues.EVENT_CLICK
        ), encodedParams.toTypedArray())
    }

    /**
     * Tracks search submit events.
     *
     * Example:
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
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(groupId = resultGroup?.groupId, groupDisplayName = resultGroup?.displayName)

        return dataManager.trackSearchSubmit(searchTerm, arrayOf(
                Constants.QueryConstants.ORIGINAL_QUERY to originalQuery,
                Constants.QueryConstants.EVENT to Constants.QueryValues.EVENT_SEARCH
        ), encodedParams.toTypedArray())
    }

    /**
     * @deprecated Prefer [trackSearchResultsLoaded] with an array of [TrackingItem] in place of an array of strings for customerIds.
     * Tracks search results loaded (a.k.a. search results viewed) events.
     *
     * Example:
     * ```
     * ConstructorIo.trackSearchResultsLoaded("tooth", 789, arrayOf("1234567-AB", "1234567-AB"))
     * ```
     * @param term the term that results are displayed for, i.e. "Pumpkin"
     * @param resultCount the number of results for that term
     * @param customerIds the customerIds of shown items
     * @param analyticsTags Additional analytics tags to pass
     */
    fun trackSearchResultsLoaded(term: String, resultCount: Int, customerIds: Array<String>? = null, analyticsTags: Map<String, String>? = null) {
        var completable = trackSearchResultsLoadedInternal(term, resultCount, customerIds = customerIds, analyticsTags = analyticsTags)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, { t -> e("Search Results Loaded error: ${t.message}") }))
    }

    /**
     * Tracks search results loaded (a.k.a. search results viewed) events.
     *
     * Example:
     * ```
     * ConstructorIo.trackSearchResultsLoaded("tooth", 789, arrayOf(TrackingItem("1234", "2345", "camp1234", "owner-A"))
     * ```
     * @param term the term that results are displayed for, i.e. "Pumpkin"
     * @param resultCount the number of results for that term
     * @param items the list of items shown
     * @param analyticsTags Additional analytics tags to pass
     */
    fun trackSearchResultsLoaded(term: String, resultCount: Int, items: Array<TrackingItem>, analyticsTags: Map<String, String>? = null) {
        var completable = trackSearchResultsLoadedInternal(term, resultCount, items = items, analyticsTags = analyticsTags)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, { t -> e("Search Results Loaded error: ${t.message}") }))
    }

    internal fun trackSearchResultsLoadedInternal(term: String, resultCount: Int, customerIds: Array<String>? = null, items: Array<TrackingItem>? = null, analyticsTags: Map<String, String>? = null): Completable {
        preferenceHelper.getSessionId(sessionIncrementHandler)
        val itemsList: List<TrackingItem>? = when {
            items != null -> items.toList()
            customerIds != null -> customerIds.map { id -> TrackingItem(id, null, null, null) }
            else -> null
        }
        val searchResultLoadRequestBody = SearchResultLoadRequestBody(
                term,
                itemsList,
                resultCount,
                "Not Available",
                BuildConfig.CLIENT_VERSION,
                preferenceHelper.id,
                preferenceHelper.getSessionId(),
                preferenceHelper.apiKey,
                configMemoryHolder.userId,
                configMemoryHolder.segments,
                mergeAnalyticsTags(configMemoryHolder.defaultAnalyticsTags, analyticsTags),
                true,
                preferenceHelper.defaultItemSection,
                System.currentTimeMillis()
        )

        return dataManager.trackSearchResultsLoaded(
                searchResultLoadRequestBody,
                arrayOf()
        )
    }

    /**
     * Tracks search result click events.
     *
     * Example:
     * ```
     * ConstructorIo.trackSearchResultClick("Fashionable Toothpicks", "1234567-AB", "tooth", "Products", "179b8a0e-3799-4a31-be87-127b06871de2")
     * ```
     * @param itemName the name of the clicked item i.e. "Kabocha Pumpkin"
     * @param customerId the identifier of the clicked item i.e "PUMP-KAB-0002"
     * @param searchTerm the term that results are displayed for, i.e. "Pumpkin"
     * @param sectionName the section that the results came from, i.e. "Products"
     * @param resultID the result ID of the search response that the click came from
     * @param slCampaignId The campaign id of the clicked item
     * @param slCampaignOwner The campaign owner of the clicked item
   */
    fun trackSearchResultClick(itemName: String, customerId: String, searchTerm: String = Constants.QueryConstants.TERM_UNKNOWN, sectionName: String? = null, resultID: String? = null, slCampaignId: String? = null, slCampaignOwner: String? = null) {
        var completable = trackSearchResultClickInternal(itemName, customerId, null, searchTerm, sectionName, resultID, slCampaignId, slCampaignOwner)

        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Search Result Click error: ${t.message}")
        }))
    }

    /**
     * Tracks search result click events.
     *
     * Example:
     * ```
     * ConstructorIo.trackSearchResultClick("Fashionable Toothpicks", "1234567-AB", "1234567-AB-RED", "tooth", "Products", "179b8a0e-3799-4a31-be87-127b06871de2")
     * ```
     * @param itemName the name of the clicked item i.e. "Kabocha Pumpkin"
     * @param customerId the identifier of the clicked item i.e "PUMP-KAB-0002"
     * @param variationId the variation identifier of the clicked item variation i.e "PUMP-KAB-0002-RED"
     * @param searchTerm the term that results are displayed for, i.e. "Pumpkin"
     * @param sectionName the section that the results came from, i.e. "Products"
     * @param resultID the result ID of the search response that the click came from
     * @param slCampaignId The campaign id of the clicked item
     * @param slCampaignOwner The campaign owner of the clicked item
     */
    fun trackSearchResultClick(itemName: String, customerId: String, variationId: String?, searchTerm: String = Constants.QueryConstants.TERM_UNKNOWN, sectionName: String? = null, resultID: String? = null, slCampaignId: String? = null, slCampaignOwner: String? = null) {
        var completable = trackSearchResultClickInternal(itemName, customerId, variationId, searchTerm, sectionName, resultID, slCampaignId, slCampaignOwner)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Search Result Click error: ${t.message}")
        }))
    }

    internal fun trackSearchResultClickInternal(itemName: String, customerId: String, variationId: String?, searchTerm: String = Constants.QueryConstants.TERM_UNKNOWN, sectionName: String? = null, resultID: String? = null, slCampaignId: String? = null, slCampaignOwner: String? = null): Completable {
        preferenceHelper.getSessionId(sessionIncrementHandler)
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(resultId = resultID, slCampaignId = slCampaignId, slCampaignOwner = slCampaignOwner)
        val sName = sectionName ?: preferenceHelper.defaultItemSection
        return dataManager.trackSearchResultClick(itemName, customerId, variationId, searchTerm, arrayOf(
                Constants.QueryConstants.SECTION to sName
        ), encodedParams.toTypedArray())

    }

    /**
     * Tracks conversion (a.k.a add to cart) events.
     *
     * Example:
     * ```
     * ConstructorIo.trackConversion("Fashionable Toothpicks", "1234567-AB", 12.99, "tooth", "Products", "add_to_cart")
     * ```
     * @param itemName the name of the converting item i.e. "Kabocha Pumpkin"
     * @param customerId the identifier of the converting item i.e "PUMP-KAB-0002"
     * @param searchTerm the search term that lead to the event (if adding to cart in a search flow)
     * @param sectionName the section that the results came from, i.e. "Products"
     * @param conversionType the type of conversion, i.e. "add_to_cart"
     * @param isCustomType if the conversion is custom (required for displayName)
     * @param displayName display name of custom conversion type (required for isCustomType)
     * @param analyticsTags Additional analytics tags to pass
     */
    fun trackConversion(itemName: String, customerId: String, revenue: Double?, searchTerm: String = Constants.QueryConstants.TERM_UNKNOWN, sectionName: String? = null, conversionType: String? = null, isCustomType: Boolean? = null, displayName: String? = null, analyticsTags: Map<String, String>? = null) {
        var completable = trackConversionInternal(itemName, customerId, null, revenue, searchTerm, sectionName, conversionType, isCustomType, displayName, analyticsTags)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Conversion error: ${t.message}")
        }))
    }

    /**
     * Tracks conversion (a.k.a add to cart) events.
     *
     * Example:
     * ```
     * ConstructorIo.trackConversion("Fashionable Toothpicks", "1234567-AB", "1234567-AB-RED", 12.99, "tooth", "Products", "add_to_cart")
     * ```
     * @param itemName the name of the converting item i.e. "Kabocha Pumpkin"
     * @param customerId the identifier of the converting item i.e "PUMP-KAB-0002"
     * @param variationId the variation identifier of the clicked item variation i.e "PUMP-KAB-0002-RED"
     * @param searchTerm the search term that lead to the event (if adding to cart in a search flow)
     * @param sectionName the section that the results came from, i.e. "Products"
     * @param conversionType the type of conversion, i.e. "add_to_cart"
     * @param isCustomType if the conversion is custom (required for displayName)
     * @param displayName display name of custom conversion type (required for isCustomType)
     * @param analyticsTags Additional analytics tags to pass
     */
    fun trackConversion(itemName: String, customerId: String, variationId: String?, revenue: Double?, searchTerm: String = Constants.QueryConstants.TERM_UNKNOWN, sectionName: String? = null, conversionType: String? = null, isCustomType: Boolean? = null, displayName: String? = null, analyticsTags: Map<String, String>? = null) {
        var completable = trackConversionInternal(itemName, customerId, variationId, revenue, searchTerm, sectionName, conversionType, isCustomType, displayName, analyticsTags)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Conversion error: ${t.message}")
        }))
    }

    internal fun trackConversionInternal(itemName: String, customerId: String, variationId: String?, revenue: Double?, searchTerm: String = Constants.QueryConstants.TERM_UNKNOWN, sectionName: String? = null, conversionType: String? = null, isCustomType: Boolean? = null, displayName: String? = null, analyticsTags: Map<String, String>? = null): Completable {
        preferenceHelper.getSessionId(sessionIncrementHandler)
        val section = sectionName ?: preferenceHelper.defaultItemSection
        val conversionRequestBody = ConversionRequestBody(
                searchTerm,
                customerId,
                variationId,
                itemName,
                String.format("%.2f", revenue),
                conversionType,
                isCustomType,
                displayName,
                BuildConfig.CLIENT_VERSION,
                preferenceHelper.id,
                preferenceHelper.getSessionId(),
                preferenceHelper.apiKey,
                configMemoryHolder.userId,
                configMemoryHolder.segments,
                mergeAnalyticsTags(configMemoryHolder.defaultAnalyticsTags, analyticsTags),
                true,
                section,
                System.currentTimeMillis()
        )

        return dataManager.trackConversion(conversionRequestBody, arrayOf())
    }

    /**
     * Tracks purchase events.
     *
     * Example:
     * ```
     * ConstructorIo.trackPurchase(arrayOf("1234567-AB", "1234567-AB"), 25.98, "ORD-1312343")
     * ```
     * @param customerIds the identifiers of the purchased items
     * @param revenue the revenue of the purchase event
     * @param orderID the identifier of the order
     * @param analyticsTags Additional analytics tags to pass
   */
    fun trackPurchase(customerIds: Array<String>, revenue: Double?, orderID: String, sectionName: String? = null, analyticsTags: Map<String, String>? = null) {
        val items = customerIds.map { item -> PurchaseItem(item) }
        var completable = trackPurchaseInternal(items.toTypedArray(), revenue, orderID, sectionName, analyticsTags)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Purchase error: ${t.message}")
        }))
    }

    /**
     * Tracks purchase events.
     *
     * Example:
     * ```
     * ConstructorIo.trackPurchase(arrayOf(PurchaseItem("1234567-AB", "1234567-AB-RED")), 25.98, "ORD-1312343")
     * ```
     * @param items the purchased items
     * @param revenue the revenue of the purchase event
     * @param orderID the identifier of the order
     * @param analyticsTags Additional analytics tags to pass
     */
    fun trackPurchase(items: Array<PurchaseItem>, revenue: Double?, orderID: String, sectionName: String? = null, analyticsTags: Map<String, String>? = null) {
        var completable = trackPurchaseInternal(items, revenue, orderID, sectionName, analyticsTags)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Purchase error: ${t.message}")
        }))
    }

    internal fun trackPurchaseInternal(items: Array<PurchaseItem>, revenue: Double?, orderID: String, sectionName: String? = null, analyticsTags: Map<String, String>? = null): Completable {
        var itemsCopy: MutableList<PurchaseItem> = ArrayList()
        for (item in items) {
            repeat(item.quantity) { itemsCopy.add(item) }
        }
        preferenceHelper.getSessionId(sessionIncrementHandler)
        val sectionNameParam = sectionName ?: preferenceHelper.defaultItemSection
        val params = mutableListOf(Constants.QueryConstants.SECTION to sectionNameParam)
        val purchaseRequestBody = PurchaseRequestBody(
                itemsCopy,
                orderID,
                revenue,
                BuildConfig.CLIENT_VERSION,
                preferenceHelper.id,
                preferenceHelper.getSessionId(),
                configMemoryHolder.userId,
                configMemoryHolder.segments,
                mergeAnalyticsTags(configMemoryHolder.defaultAnalyticsTags, analyticsTags),
                preferenceHelper.apiKey,
                true,
                System.currentTimeMillis()
        )

        return dataManager.trackPurchase(purchaseRequestBody, params.toTypedArray())
    }

    /**
     * @deprecated Prefer [trackBrowseResultsLoaded] with an array of [TrackingItem] in place of an array of strings for itemIds.
     * Tracks browse result loaded (a.k.a. browse results viewed) events.
     *
     * Example:
     * ```
     * ConstructorIo.trackBrowseResultsLoaded("Category", "Snacks", arrayOf("123", "234"), 674)
     * ```
     * @param filterName the name of the primary filter, i.e. "Aisle"
     * @param filterValue the value of the primary filter, i.e. "Produce"
     * @param itemIds the item ids of the displayed items
     * @param resultCount the number of results for that filter name/value pair
     * @param analyticsTags Additional analytics tags to pass
     */
    fun trackBrowseResultsLoaded(filterName: String, filterValue: String, itemIds: Array<String>, resultCount: Int, sectionName: String? = null, url: String = "Not Available", analyticsTags: Map<String, String>? = null) {
        var completable = trackBrowseResultsLoadedInternal(filterName, filterValue, itemIds = itemIds, resultCount = resultCount, sectionName = sectionName, url = url, analyticsTags = analyticsTags)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, { t -> e("Browse Results Loaded error: ${t.message}") }))
    }

    /**
     * Tracks browse result loaded (a.k.a. browse results viewed) events.
     *
     * Example:
     * ```
     * ConstructorIo.trackBrowseResultsLoaded("Category", "Snacks", arrayOf(TrackingItem("1234", "2345", "camp1234", "owner-A")), 674)
     * ```
     * @param filterName the name of the primary filter, i.e. "Aisle"
     * @param filterValue the value of the primary filter, i.e. "Produce"
     * @param items the list of the displayed items
     * @param resultCount the number of results for that filter name/value pair
     * @param analyticsTags Additional analytics tags to pass
     */
    fun trackBrowseResultsLoaded(filterName: String, filterValue: String, items: Array<TrackingItem>, resultCount: Int, sectionName: String? = null, url: String = "Not Available", analyticsTags: Map<String, String>? = null) {
        var completable = trackBrowseResultsLoadedInternal(filterName, filterValue, items = items, resultCount = resultCount, sectionName = sectionName, url = url, analyticsTags = analyticsTags)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, { t -> e("Browse Results Loaded error: ${t.message}") }))
    }

    /**
     * Tracks browse result loaded (a.k.a. browse results viewed) events.
     *
     * Example:
     * ```
     * ConstructorIo.trackBrowseResultsLoaded("Category", "Snacks", 674)
     * ```
     * @param filterName the name of the primary filter, i.e. "Aisle"
     * @param filterValue the value of the primary filter, i.e. "Produce"
     * @param resultCount the number of results for that filter name/value pair
     * @param analyticsTags Additional analytics tags to pass
     */
    fun trackBrowseResultsLoaded(filterName: String, filterValue: String, resultCount: Int, sectionName: String? = null, url: String = "Not Available", analyticsTags: Map<String, String>? = null) {
        var completable = trackBrowseResultsLoadedInternal(filterName, filterValue, null, null, resultCount, sectionName, url, analyticsTags)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Browse Results Loaded error: ${t.message}")
        }))
    }

    internal fun trackBrowseResultsLoadedInternal(filterName: String, filterValue: String, itemIds: Array<String>? = null, items: Array<TrackingItem>? = null, resultCount: Int, sectionName: String? = null, url: String = "Not Available", analyticsTags: Map<String, String>? = null): Completable {
        preferenceHelper.getSessionId(sessionIncrementHandler)
        val section = sectionName ?: preferenceHelper.defaultItemSection
        val itemsList: List<TrackingItem>? = when {
            items != null -> items.toList()
            itemIds != null -> itemIds.map { id -> TrackingItem(id, null, null, null) }
            else -> null
        }
        val browseResultLoadRequestBody = BrowseResultLoadRequestBody(
                filterName,
                filterValue,
                itemsList,
                resultCount,
                url,
                BuildConfig.CLIENT_VERSION,
                preferenceHelper.id,
                preferenceHelper.getSessionId(),
                preferenceHelper.apiKey,
                configMemoryHolder.userId,
                configMemoryHolder.segments,
                mergeAnalyticsTags(configMemoryHolder.defaultAnalyticsTags, analyticsTags),
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
     * Tracks browse result click events.
     *
     * Example:
     * ```
     * ConstructorIo.trackBrowseResultClick("Category", "Snacks", "7654321-BA", "4", "Products", "179b8a0e-3799-4a31-be87-127b06871de2")
     * ```
     * @param filterName the name of the primary filter, i.e. "Aisle"
     * @param filterValue the value of the primary filter, i.e. "Produce"
     * @param customerId the item identifier of the clicked item i.e "PUMP-KAB-0002"
     * @param resultPositionOnPage the position of the clicked item on the page i.e. 4
     * @param sectionName the section that the results came from, i.e. "Products"
     * @param resultID the result ID of the browse response that the selection came from
     * @param analyticsTags Additional analytics tags to pass
     * @param slCampaignId The campaign id of the clicked item
     * @param slCampaignOwner The campaign owner of the clicked item
     */
    fun trackBrowseResultClick(filterName: String, filterValue: String, customerId: String, resultPositionOnPage: Int, sectionName: String? = null, resultID: String? = null, analyticsTags: Map<String, String>? = null, slCampaignId: String? = null, slCampaignOwner: String? = null) {
        var completable = trackBrowseResultClickInternal(filterName, filterValue, customerId, null, resultPositionOnPage, sectionName, resultID, slCampaignId, slCampaignOwner, analyticsTags)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Browse Result Click error: ${t.message}")
        }))
    }

    /**
     * Tracks browse result click events.
     *
     * Example:
     * ```
     * ConstructorIo.trackBrowseResultClick("Category", "Snacks", "7654321-BA", "7654321-BA-RED", "4", "Products", "179b8a0e-3799-4a31-be87-127b06871de2")
     * ```
     * @param filterName the name of the primary filter, i.e. "Aisle"
     * @param filterValue the value of the primary filter, i.e. "Produce"
     * @param customerId the item identifier of the clicked item i.e "PUMP-KAB-0002"
     * @param variationId the variation identifier of the clicked item variation i.e "PUMP-KAB-0002-RED"
     * @param resultPositionOnPage the position of the clicked item on the page i.e. 4
     * @param sectionName the section that the results came from, i.e. "Products"
     * @param resultID the result ID of the browse response that the selection came from
     * @param analyticsTags Additional analytics tags to pass
     * @param slCampaignId The campaign id of the clicked item
     * @param slCampaignOwner The campaign owner of the clicked item
     */
    fun trackBrowseResultClick(filterName: String, filterValue: String, customerId: String, variationId: String, resultPositionOnPage: Int, sectionName: String? = null, resultID: String? = null, analyticsTags: Map<String, String>? = null, slCampaignId: String? = null, slCampaignOwner: String? = null) {
        var completable = trackBrowseResultClickInternal(filterName, filterValue, customerId, variationId, resultPositionOnPage, sectionName, resultID, slCampaignId, slCampaignOwner, analyticsTags)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Browse Result Click error: ${t.message}")
        }))
    }

    internal fun trackBrowseResultClickInternal(filterName: String, filterValue: String, customerId: String, variationId: String? = null, resultPositionOnPage: Int, sectionName: String? = null, resultID: String? = null, slCampaignId: String? = null, slCampaignOwner: String? = null, analyticsTags: Map<String, String>? = null): Completable {
        preferenceHelper.getSessionId(sessionIncrementHandler)
        val section = sectionName ?: preferenceHelper.defaultItemSection
        val browseResultClickRequestBody = BrowseResultClickRequestBody(
                filterName,
                filterValue,
                customerId,
                variationId,
                resultPositionOnPage,
                BuildConfig.CLIENT_VERSION,
                preferenceHelper.id,
                preferenceHelper.getSessionId(),
                preferenceHelper.apiKey,
                configMemoryHolder.userId,
                configMemoryHolder.segments,
                mergeAnalyticsTags(configMemoryHolder.defaultAnalyticsTags, analyticsTags),
                true,
                section,
                System.currentTimeMillis(),
                resultID,
                slCampaignId,
                slCampaignOwner,
        )

        return dataManager.trackBrowseResultClick(
                browseResultClickRequestBody,
                arrayOf(Constants.QueryConstants.SECTION to section),
        )

    }

    /**
     * Tracks item result loaded events.
     *
     * Example:
     * ```
     * ConstructorIo.trackItemDetailLoaded("Pencil", "123", "234")
     * ```
     * @param itemName the name of the item loaded
     * @param customerId the id of the item loaded
     * @param variationId the variationId of the item loaded
     * @param sectionName section of the item loaded
     * @param url the url of the item
     * @param analyticsTags Additional analytics tags to pass
     */
    fun trackItemDetailLoaded(itemName: String, customerId: String, variationId: String? = null, sectionName: String? = null, url: String = "Not Available", analyticsTags: Map<String, String>? = null) {
        var completable = trackItemDetailLoadedInternal(itemName, customerId, variationId, sectionName, url, analyticsTags)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Item Detail Loaded error: ${t.message}")
        }))
    }

    internal fun trackItemDetailLoadedInternal(itemName: String, customerId: String, variationId: String? = null, sectionName: String? = null, url: String = "Not Available", analyticsTags: Map<String, String>? = null): Completable {
        preferenceHelper.getSessionId(sessionIncrementHandler)
        val section = sectionName ?: preferenceHelper.defaultItemSection
        val itemDetailLoadRequestBody = ItemDetailLoadRequestBody(
                itemName,
                customerId,
                variationId,
                url,
                BuildConfig.CLIENT_VERSION,
                preferenceHelper.id,
                preferenceHelper.getSessionId(),
                preferenceHelper.apiKey,
                configMemoryHolder.userId,
                configMemoryHolder.segments,
                mergeAnalyticsTags(configMemoryHolder.defaultAnalyticsTags, analyticsTags),
                true,
                section,
                System.currentTimeMillis()
        )

        return dataManager.trackItemDetailLoaded(
                itemDetailLoadRequestBody,
                arrayOf(Constants.QueryConstants.SECTION to section),
        )
    }

    /**
     * Tracks generic result click events.
     *
     * Example:
     * ```
     * ConstructorIo.trackGenericResultClick("Pencil", "123", "234")
     * ```
     * @param itemName the name of the item clicked
     * @param customerId the id of the item clicked
     * @param variationId the variationId of the item clicked
     * @param sectionName section of the item clicked
     * @param analyticsTags Additional analytics tags to pass
     */
    fun trackGenericResultClick(itemName: String, customerId: String, variationId: String? = null, sectionName: String? = null, analyticsTags: Map<String, String>? = null) {
        var completable = trackGenericResultClickInternal(itemName, customerId, variationId, sectionName, analyticsTags)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Generic Result Click error: ${t.message}")
        }))
    }

    internal fun trackGenericResultClickInternal(itemName: String, customerId: String, variationId: String? = null, sectionName: String? = null, analyticsTags: Map<String, String>? = null) : Completable {
        preferenceHelper.getSessionId(sessionIncrementHandler)
        val section = sectionName ?: preferenceHelper.defaultItemSection
        val genericResultClickRequestBody = GenericResultClickRequestBody(
                itemName,
                customerId,
                variationId,
                BuildConfig.CLIENT_VERSION,
                preferenceHelper.id,
                preferenceHelper.getSessionId(),
                preferenceHelper.apiKey,
                configMemoryHolder.userId,
                configMemoryHolder.segments,
                mergeAnalyticsTags(configMemoryHolder.defaultAnalyticsTags, analyticsTags),
            true,
                section,
                System.currentTimeMillis()
        )

        return dataManager.trackGenericResultClick(
                genericResultClickRequestBody,
                arrayOf(Constants.QueryConstants.SECTION to section),
        )
    }

    /**
     * Returns a list of recommendation results for the specified pod.
     *
     * Example:
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
     * @param variationsMap specify which attributes within variations should be returned
     * @param preFilterExpression faceting expression to scope results
     * @param hiddenFields show fields that are hidden by default
     */
    fun getRecommendationResults(podId: String, facets: List<Pair<String, List<String>>>? = null, numResults: Int? = null, sectionName: String? = null, itemId: String? = null, term: String? = null, variationsMap: VariationsMap? = null, preFilterExpression: String? = null, hiddenFields: List<String>? = null): Observable<ConstructorData<RecommendationsResponse>> {
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(numResults = numResults, sectionName = sectionName, itemId = itemId, term = term, facets = facets, variationsMap = variationsMap, preFilterExpression = preFilterExpression, hiddenFields = hiddenFields)

        return dataManager.getRecommendationResults(podId, encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Returns a list of recommendation results including filters, categories, sort options, etc.
     *
     * Example:
     * ```
     *  runBlocking {
     *      launch {
     *          try {
     *              val recommendationResults = constructorIo.getRecommendationResultsCRT("product_detail_page")
     *              // Do something with recommendationResults
     *          } catch (e: Exception) {
     *              println(e)
     *          }
     *      }
     *  }
     * ```
     * @param podId the pod id
     * @param facets  additional facets used to refine results
     * @param numResults the number of results to return
     * @param sectionName the section the selection will come from, i.e. "Products"
     * @param itemId: The item id to retrieve recommendations (strategy specific)
     * @param term: The term to use to refine results (strategy specific)
     * @param variationsMap specify which attributes within variations should be returned
     * @param preFilterExpression faceting expression to scope results
     */
    suspend fun getRecommendationResultsCRT(podId: String, facets: List<Pair<String, List<String>>>? = null, numResults: Int? = null, sectionName: String? = null, itemId: String? = null, term: String? = null, variationsMap: VariationsMap? = null, preFilterExpression: String? = null, hiddenFields: List<String>? = null): RecommendationsResponse {
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(numResults = numResults, sectionName = sectionName, itemId = itemId, term = term, facets = facets, variationsMap = variationsMap, preFilterExpression = preFilterExpression, hiddenFields = hiddenFields)

        return dataManager.getRecommendationResultsCRT(podId, encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Returns a list of recommendation results for the specified pod.
     *
     * Example:
     * ```
     * val request = RecommendationsRequest.Builder("product_detail_page")
     *      .setItemIds(listOf("item_id_123"))
     *      .build()
     *
     * ConstructorIo.getRecommendationResults(request)
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
     * @param podId the pod id
     * @param filters additional filters used to refine results
     * @param itemIds the list of item ids to retrieve recommendations for (strategy specific)
     * @param term the term to use to refine results (strategy specific)
     * @param numResults the number of results to return
     * @param section the section the results will come from, i.e. "Products"
     * @param variationsMap specify which attributes within variations should be returned
     * @param preFilterExpression faceting expression to scope results
     */
    fun getRecommendationResults(request: RecommendationsRequest): Observable<ConstructorData<RecommendationsResponse>> {
        val encodedParams: ArrayList<Pair<String, String>> = getEncodedParams(facets = request.filters?.toList(), itemIds = request.itemIds, term = request.term, numResults = request.numResults, sectionName = request.section, variationsMap = request.variationsMap, preFilterExpression = request.preFilterExpression, hiddenFields = request.hiddenFields)

        return dataManager.getRecommendationResults(request.podId, encodedParams = encodedParams.toTypedArray())
    }

    /**
     * Tracks recommendation result click events.
     *
     * Example:
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
     * @param analyticsTags Additional analytics tags to pass
     */
    fun trackRecommendationResultClick(podId: String, strategyId: String, customerId: String, variationId: String? = null, sectionName: String? = null, resultId: String? = null, numResultsPerPage: Int? = null, resultPage: Int? = null, resultCount: Int? = null, resultPositionOnPage: Int? = null, analyticsTags: Map<String, String>? = null) {
        var completable = trackRecommendationResultClickInternal(podId, strategyId, customerId, variationId, sectionName, resultId, numResultsPerPage, resultPage, resultCount, resultPositionOnPage, analyticsTags)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Recommendation Result Click error: ${t.message}")
        }))
    }
    internal fun trackRecommendationResultClickInternal(podId: String, strategyId: String, customerId: String, variationId: String? = null, sectionName: String? = null, resultId: String? = null, numResultsPerPage: Int? = null, resultPage: Int? = null, resultCount: Int? = null, resultPositionOnPage: Int? = null, analyticsTags: Map<String, String>? = null): Completable {
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
                mergeAnalyticsTags(configMemoryHolder.defaultAnalyticsTags, analyticsTags),
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
     * Tracks recommendation result view events.
     *
     * Example:
     * ```
     * ConstructorIo.trackRecommendationResultsView("Best_Sellers", arrayOf("123", "234"), 4, 1, 4, "179b8a0e-3799-4a31-be87-127b06871de2", "Products")
     * ```
     * @param podId The pod id
     * @param itemIds the item ids of the dislayed items
     * @param numResultsViewed The count of recommendation results being viewed
     * @param resultPage The current page that recommendation result is on
     * @param resultCount The total number of recommendation results
     * @param resultId The result ID of the recommendation response that the selection came from
     * @param sectionName The section that the results came from, i.e. "Products"
     * @param analyticsTags Additional analytics tags to pass
     */
    fun trackRecommendationResultsView(podId: String, itemIds: Array<String>, numResultsViewed: Int, resultPage: Int? = null, resultCount: Int? = null, resultId: String? = null, sectionName: String? = null, url: String = "Not Available", analyticsTags: Map<String, String>? = null) {
        var completable = trackRecommendationResultsViewInternal(podId, itemIds, numResultsViewed, resultPage, resultCount, resultId, sectionName, url, analyticsTags)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
                t -> e("Recommendation Results View error: ${t.message}")
        }))
    }

    /**
     * Tracks recommendation result view events.
     *
     * Example:
     * ```
     * ConstructorIo.trackRecommendationResultsView("Best_Sellers", 4, 1, 4, "179b8a0e-3799-4a31-be87-127b06871de2", "Products")
     * ```
     * @param podId The pod id
     * @param numResultsViewed The count of recommendation results being viewed
     * @param resultPage The current page that recommendation result is on
     * @param resultCount The total number of recommendation results
     * @param resultId The result ID of the recommendation response that the selection came from
     * @param sectionName The section that the results came from, i.e. "Products"
     * @param analyticsTags Additional analytics tags to pass
     */
    fun trackRecommendationResultsView(podId: String, numResultsViewed: Int, resultPage: Int? = null, resultCount: Int? = null, resultId: String? = null, sectionName: String? = null, url: String = "Not Available", analyticsTags: Map<String, String>? = null) {
        var completable = trackRecommendationResultsViewInternal(podId, null, numResultsViewed, resultPage, resultCount, resultId, sectionName, url, analyticsTags)
        disposable.add(completable.subscribeOn(Schedulers.io()).subscribe({}, {
            t -> e("Recommendation Results View error: ${t.message}")
        }))
    }
    internal fun trackRecommendationResultsViewInternal(podId: String, itemIds: Array<String>? = null, numResultsViewed: Int, resultPage: Int? = null, resultCount: Int? = null, resultId: String? = null, sectionName: String? = null, url: String = "Not Available", analyticsTags: Map<String, String>? = null): Completable {
        preferenceHelper.getSessionId(sessionIncrementHandler)
        val section = sectionName ?: preferenceHelper.defaultItemSection
        val items = itemIds?.map{ item -> TrackingItem(item, null, null, null)}
        val recommendationResultViewRequestBody = RecommendationResultViewRequestBody(
                podId,
                items,
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
                mergeAnalyticsTags(configMemoryHolder.defaultAnalyticsTags, analyticsTags),
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
