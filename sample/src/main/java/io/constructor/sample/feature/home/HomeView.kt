package io.constructor.sample.feature.home

import io.constructor.sample.common.BaseView

interface HomeView : BaseView {
    fun renderAutcompleteData(it: List<Suggestion>)
}