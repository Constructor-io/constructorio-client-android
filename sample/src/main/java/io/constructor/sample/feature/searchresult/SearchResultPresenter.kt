package io.constructor.sample.feature.searchresult

import io.constructor.core.ConstructorIo
import io.constructor.data.model.search.SearchResponseInner
import io.constructor.data.model.common.FilterFacet
import io.constructor.data.model.common.FilterSortOption
import io.constructor.sample.common.BasePresenter
import io.constructor.sample.extensions.io2ui
import io.constructor.sample.extensions.plusAssign

class SearchResultPresenter(view: SearchView) : BasePresenter<SearchView>(view) {

    var availableFacets: List<FilterFacet>? = null
    var availableFilterSortOptions: List<FilterSortOption>? = null
    private var page = 1
    private val limit = 10
    private lateinit var query: String
    var selectedFacets: HashMap<String, MutableList<String>>? = null
    var selectedFilterSortOption: FilterSortOption? = null

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
        compositeDisposable += ConstructorIo.getSearchResults(query, selectedFacets?.map { it.key to it.value }, page = page, perPage = limit, sortBy = selectedFilterSortOption?.sortBy, sortOrder = selectedFilterSortOption?.sortOrder).io2ui().subscribe {
            it.onValue {
                if (firstRun) {
                    availableFacets = it.searchData.facets
                    availableFilterSortOptions = it.searchData.filterSortOptions
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

    fun handleClick(it: SearchResponseInner, query: String) {
        ConstructorIo.trackSearchResultClick(it.value, it.result.id, query)
        view.navigateToDetails(it)
    }

    fun facetsSelected(facets: HashMap<String, MutableList<String>>) {
        page = 1
        selectedFacets = facets
        view.clearData()
        loadNextDataBatch()
    }

    fun sortOptionSelected(optionFilter: FilterSortOption?) {
        page = 1
        selectedFilterSortOption = optionFilter
        view.clearData()
        loadNextDataBatch()
    }
}