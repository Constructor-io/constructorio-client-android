package io.constructor.data.builder

import io.constructor.data.model.common.TrackingItem

/**
 * Create a Search Results Loaded tracking request object utilizing a builder. Sent when search results are loaded into view.
 *
 * @property term the term that results are displayed for, i.e. "Pumpkin"
 * @property resultCount the total number of results for the term
 * @property items the items shown in the results
 * @property sectionName the index section the results came from, i.e. "Products". Defaults to [io.constructor.core.ConstructorIoConfig.defaultItemSection]
 * @property url the URL of the search results page. Defaults to "Not Available"
 * @property analyticsTags additional analytics tags to pass. Merged with [io.constructor.core.ConstructorIoConfig.defaultAnalyticsTags], with request-level values overriding defaults on key collision
 * @property resultId the result ID of the search response that the results came from
 * @property resultPage the current page of results. Pass either [resultPage] or [resultOffset], not both: the endpoint rejects requests carrying both
 * @property resultOffset the current offset of results, used on infinite-scroll pages. Pass either [resultPage] or [resultOffset], not both: the endpoint rejects requests carrying both
 * @property sortOrder the sort order the user had applied to the results, i.e. "ascending"
 * @property sortBy the field the user had sorted the results by, i.e. "price"
 * @property selectedFilters the filters the user had selected on the results page, keyed by facet name with the selected options as values
 */
class SearchResultsLoadedData(
    val term: String,
    val resultCount: Int,
    val items: List<TrackingItem>? = null,
    val sectionName: String? = null,
    val url: String = "Not Available",
    val analyticsTags: Map<String, String>? = null,
    val resultId: String? = null,
    val resultPage: Int? = null,
    val resultOffset: Int? = null,
    val sortOrder: String? = null,
    val sortBy: String? = null,
    val selectedFilters: Map<String, List<String>>? = null,
) {
    private constructor(builder: Builder) : this(
        builder.term,
        builder.resultCount,
        builder.items,
        builder.sectionName,
        builder.url,
        builder.analyticsTags,
        builder.resultId,
        builder.resultPage,
        builder.resultOffset,
        builder.sortOrder,
        builder.sortBy,
        builder.selectedFilters,
    )

    companion object {
        /**
         * Builds a [SearchResultsLoadedData] from the required parameters, applying [block] to set optional parameters
         */
        inline fun build(term: String, resultCount: Int, block: Builder.() -> Unit = {}) = Builder(term, resultCount).apply(block).build()
    }

    /**
     * Builder for [SearchResultsLoadedData]
     */
    class Builder(
        val term: String,
        val resultCount: Int
    ) {
        /** The items shown in the results */
        var items: List<TrackingItem>? = null
        /** The index section the results came from, i.e. "Products". Defaults to [io.constructor.core.ConstructorIoConfig.defaultItemSection] */
        var sectionName: String? = null
        /** The URL of the search results page. Defaults to "Not Available" */
        var url: String = "Not Available"
        /** Additional analytics tags to pass. Merged with [io.constructor.core.ConstructorIoConfig.defaultAnalyticsTags], with request-level values overriding defaults on key collision */
        var analyticsTags: Map<String, String>? = null
        /** The result ID of the search response that the results came from */
        var resultId: String? = null
        /** The current page of results. Pass either [resultPage] or [resultOffset], not both: the endpoint rejects requests carrying both */
        var resultPage: Int? = null
        /** The current offset of results, used on infinite-scroll pages. Pass either [resultPage] or [resultOffset], not both: the endpoint rejects requests carrying both */
        var resultOffset: Int? = null
        /** The sort order the user had applied to the results, i.e. "ascending" */
        var sortOrder: String? = null
        /** The field the user had sorted the results by, i.e. "price" */
        var sortBy: String? = null
        /** The filters the user had selected on the results page, keyed by facet name with the selected options as values */
        var selectedFilters: Map<String, List<String>>? = null

        /** Sets [items] */
        fun setItems(items: List<TrackingItem>): Builder = apply { this.items = items }
        /** Sets [sectionName] */
        fun setSectionName(sectionName: String): Builder = apply { this.sectionName = sectionName }
        /** Sets [url] */
        fun setUrl(url: String): Builder = apply { this.url = url }
        /** Sets [analyticsTags] */
        fun setAnalyticsTags(analyticsTags: Map<String, String>): Builder = apply { this.analyticsTags = analyticsTags }
        /** Sets [resultId] */
        fun setResultId(resultId: String): Builder = apply { this.resultId = resultId }
        /** Sets [resultPage]. Pass either [resultPage] or [resultOffset], not both */
        fun setResultPage(resultPage: Int): Builder = apply { this.resultPage = resultPage }
        /** Sets [resultOffset]. Pass either [resultPage] or [resultOffset], not both */
        fun setResultOffset(resultOffset: Int): Builder = apply { this.resultOffset = resultOffset }
        /** Sets [sortOrder] */
        fun setSortOrder(sortOrder: String): Builder = apply { this.sortOrder = sortOrder }
        /** Sets [sortBy] */
        fun setSortBy(sortBy: String): Builder = apply { this.sortBy = sortBy }
        /** Sets [selectedFilters] */
        fun setSelectedFilters(selectedFilters: Map<String, List<String>>): Builder = apply { this.selectedFilters = selectedFilters }

        /** Builds the [SearchResultsLoadedData] */
        fun build(): SearchResultsLoadedData = SearchResultsLoadedData(this)
    }
}
