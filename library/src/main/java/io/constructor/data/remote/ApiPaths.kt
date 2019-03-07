package io.constructor.data.remote

object ApiPaths {
    const val URL_GET_SUGGESTIONS = "autocomplete/{value}"
    const val URL_AUTOCOMPLETE_SELECT_EVENT = "autocomplete/{term}/select"
    const val URL_SEARCH_SUBMIT_EVENT = "autocomplete/{term}/getSearchResults"
    const val URL_SESSION_START_EVENT = "behavior"
    const val URL_CONVERSION_EVENT = "autocomplete/{term}/conversion"
    const val URL_SEARCH_RESULT_CLICK_EVENT = "autocomplete/{term}/click_through"
    const val URL_BEHAVIOR = "behavior"
    const val URL_PURCHASE = "autocomplete/TERM_UNKNOWN/purchase"
    const val URL_SEARCH = "search/%s"

}