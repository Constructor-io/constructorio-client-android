package io.constructor.core

import io.constructor.BuildConfig

data class ConstructorIoConfig(val apiKey: String,
                               val serviceUrl: String = BuildConfig.BASE_API_URL,
                               val autocompleteResultCount: Map<String, Int> = mapOf(Constants.QueryValues.SEARCH_SUGGESTIONS to 10, Constants.QueryValues.PRODUCTS to 0),
                               val defaultItemSection: String = BuildConfig.AUTOCOMPLETE_SECTION,
                               val testCells: List<Pair<String, String>> = emptyList())