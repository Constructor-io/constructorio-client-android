package io.constructor.sample.feature.home

import io.constructor.data.model.autocomplete.AutocompleteResponse
import io.constructor.sample.common.BaseView

interface HomeView : BaseView {
    fun renderAutocompleteData(it: AutocompleteResponse)
}