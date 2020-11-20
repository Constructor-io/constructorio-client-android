package io.constructor.core

class Constants {

    companion object {
        val EVENT_QUERY_SENT = this::class.qualifiedName + "query_sent"
        val EVENT_SUGGESTIONS_RETRIEVED = this::class.qualifiedName + "suggestions_retrieved"
        val EXTRA_QUERY = this::class.qualifiedName + "query"
        val EXTRA_TERM = this::class.qualifiedName + "term"
        val EXTRA_SUGGESTIONS = this::class.qualifiedName + "suggestions"
        val EXTRA_SUGGESTION = this::class.qualifiedName + "suggestion"
    }

    object QueryConstants {
        const val SESSION = "s"
        const val TIMESTAMP = "_dt"
        const val IDENTITY = "i"
        const val ACTION = "action"
        const val AUTOCOMPLETE_SECTION = "autocomplete_section"
        const val ORIGINAL_QUERY = "original_query"
        const val CLIENT = "c"
        const val EVENT = "tr"
        const val API_KEY = "key"
        const val NUM_RESULTS = "num_results_"
        const val CUSTOMER_ID = "customer_ids"
        const val GROUP_ID = "group[group_id]"
        const val GROUP_DISPLAY_NAME = "group[display_name]"
        const val USER_ID = "ui"
        const val TERM_UNKNOWN = "TERM_UNKNOWN"
        const val PAGE = "page"
        const val PER_PAGE = "num_results_per_page"
        const val SORT_BY = "sort_by"
        const val SORT_ORDER = "sort_order"
        const val FILTER_GROUP_ID = "filters[group_id]"
        const val FILTER_FACET = "filters[%s]"
        const val RESULT_ID = "result_id"
    }

    object QueryValues {
        const val EVENT_CLICK = "click"
        const val EVENT_SEARCH = "search"
        const val EVENT_SESSION_START = "session_start"
        const val SEARCH_SUGGESTIONS = "Search Suggestions"
        const val PRODUCTS = "Products"
        const val EVENT_SEARCH_RESULTS = "search-results"
        const val EVENT_BROWSE_RESULTS = "browse-results"
        const val EVENT_INPUT_FOCUS = "focus"
    }
}