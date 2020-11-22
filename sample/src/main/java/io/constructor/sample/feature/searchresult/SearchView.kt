package io.constructor.sample.feature.searchresult

import io.constructor.data.model.common.Result
import io.constructor.data.model.search.SearchResponseInner
import io.constructor.sample.common.BaseView

interface SearchView : BaseView {
    fun renderData(it: SearchResponseInner, totalCount: Int)
    fun navigateToDetails(it: Result)
    fun clearData()
}