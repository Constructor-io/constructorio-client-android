package io.constructor.sample.feature.searchresult

import io.constructor.data.model.search.SearchResult
import io.constructor.data.model.search.SearchData
import io.constructor.sample.common.BaseView

interface SearchView : BaseView {
    fun renderData(it: SearchData, totalCount: Int)
    fun navigateToDetails(it: SearchResult)
    fun clearData()
}