package io.constructor.sample.feature.cart

import io.constructor.data.model.search.SearchResponseInner
import io.constructor.sample.common.BaseView

interface CartView : BaseView {
    fun renderContent(content: LinkedHashMap<String, Pair<SearchResponseInner, Int>>)
}