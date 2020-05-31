package io.constructor.sample.feature.checkout

import io.constructor.data.model.search.SearchResult
import io.constructor.sample.common.BaseView

interface CheckoutView : BaseView {
    fun renderContent(content: LinkedHashMap<String, Pair<SearchResult, Int>>)
}