package io.constructor.data.builder

import io.constructor.data.model.common.TrackingItem

/**
 * Create a Search Results Loaded tracking request object utilizing a builder
 */
class SearchResultsLoadedData(
    val term: String,
    val resultCount: Int,
    val items: List<TrackingItem>? = null,
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
        builder.analyticsTags,
        builder.resultId,
        builder.resultPage,
        builder.resultOffset,
        builder.sortOrder,
        builder.sortBy,
        builder.selectedFilters,
    )

    companion object {
        inline fun build(term: String, resultCount: Int, block: Builder.() -> Unit = {}) = Builder(term, resultCount).apply(block).build()
    }

    class Builder(
        val term: String,
        val resultCount: Int
    ) {
        var items: List<TrackingItem>? = null
        var analyticsTags: Map<String, String>? = null
        var resultId: String? = null
        var resultPage: Int? = null
        var resultOffset: Int? = null
        var sortOrder: String? = null
        var sortBy: String? = null
        var selectedFilters: Map<String, List<String>>? = null

        fun setItems(items: List<TrackingItem>): Builder = apply { this.items = items }
        fun setAnalyticsTags(analyticsTags: Map<String, String>): Builder = apply { this.analyticsTags = analyticsTags }
        fun setResultId(resultId: String): Builder = apply { this.resultId = resultId }
        fun setResultPage(resultPage: Int): Builder = apply { this.resultPage = resultPage }
        fun setResultOffset(resultOffset: Int): Builder = apply { this.resultOffset = resultOffset }
        fun setSortOrder(sortOrder: String): Builder = apply { this.sortOrder = sortOrder }
        fun setSortBy(sortBy: String): Builder = apply { this.sortBy = sortBy }
        fun setSelectedFilters(selectedFilters: Map<String, List<String>>): Builder = apply { this.selectedFilters = selectedFilters }
        fun build(): SearchResultsLoadedData = SearchResultsLoadedData(this)
    }
}
