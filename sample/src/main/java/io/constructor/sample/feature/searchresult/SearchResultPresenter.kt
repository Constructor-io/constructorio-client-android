package io.constructor.sample.feature.searchresult

import io.constructor.core.ConstructorIo
import io.constructor.data.model.search.SearchResult
import io.constructor.data.model.search.SearchFacet
import io.constructor.data.model.search.SortOption
import io.constructor.sample.common.BasePresenter
import io.constructor.sample.extensions.io2ui
import io.constructor.sample.extensions.plusAssign

class SearchResultPresenter(view: SearchView) : BasePresenter<SearchView>(view) {

    var availableFacets: List<SearchFacet>? = null
    var availableSortOptions: List<SortOption>? = null
    private var page = 1
    private val limit = 10
    private lateinit var query: String
    var selectedFacets: HashMap<String, MutableList<String>>? = null
    var selectedSortOption: SortOption? = null

    private var totalCount: Int? = null

    fun onCreate(query: String) {
        this.query = query
        loadNextDataBatch()
    }

    fun loadNextDataBatch(firstRun: Boolean = true) {
        if (firstRun) {
            loadInternal(firstRun)
        } else {
            totalCount?.let {
                if ((page - 1) * limit <= it) {
                    loadInternal(firstRun)
                }
            }
        }
    }

    private fun loadInternal(firstRun: Boolean) {
        compositeDisposable += ConstructorIo.getSearchResults(query, selectedFacets?.map { it.key to it.value }, page = page, perPage = limit, sortBy = selectedSortOption?.sortBy, sortOrder = selectedSortOption?.sortOrder).io2ui().subscribe {
            it.onValue {
                if (firstRun) {
                    availableFacets = it.searchData.facets
                    availableSortOptions = it.searchData.sortOptions
                    ConstructorIo.trackSearchResultsLoaded(query, it.searchData.resultCount)
                }
                it.searchData?.let {
                    page += 1
                    totalCount = it.resultCount
                    view.renderData(it, totalCount!!)
                }
            }
        }
    }

    fun handleClick(it: SearchResult, query: String) {
        ConstructorIo.trackSearchResultClick(it.value, it.result.id, query)
        view.navigateToDetails(it)
    }

    fun facetsSelected(facets: HashMap<String, MutableList<String>>) {
        page = 1
        selectedFacets = facets
        view.clearData()
        loadNextDataBatch()
    }

    fun sortOptionSelected(option: SortOption?) {
        page = 1
        selectedSortOption = option
        view.clearData()
        loadNextDataBatch()
    }
}