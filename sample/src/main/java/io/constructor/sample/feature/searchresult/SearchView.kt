package io.constructor.sample.feature.searchresult

import io.constructor.data.model.search.SearchResponseInner
import io.constructor.sample.common.BaseView

interface SearchView : BaseView {
    fun renderData(it: SearchData, totalCount: Int)
    fun navigateToDetails(it: SearchResponseInner)
    fun clearData()
}