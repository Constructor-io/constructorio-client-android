package io.constructor.data.remote

object ApiPaths {
    const val URL_GET_SUGGESTIONS = "autocomplete/{value}"
    const val URL_SELECT_EVENT = "autocomplete/{term}/select"
    const val URL_SEARCH_EVENT = "autocomplete/{term}/search"
    const val URL_SESSION_START_EVENT = "behavior"
    const val URL_CONVERSION_EVENT = "autocomplete/{term}/conversion"
    const val URL_SEARCH_CLICK_EVENT = "autocomplete/{term}/click_through"
    const val URL_BEHAVIOR = "behavior"
    const val URL_PURCHASE = "autocomplete/TERM_UNKNOWN/purchase"

}