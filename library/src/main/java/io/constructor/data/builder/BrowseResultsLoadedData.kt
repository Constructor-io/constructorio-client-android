package io.constructor.data.builder

import io.constructor.data.model.common.TrackingItem

/**
 * Create a Browse Results Loaded tracking request object utilizing a builder
 */
class BrowseResultsLoadedData(
    val filterName: String,
    val filterValue: String,
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
        builder.filterName,
        builder.filterValue,
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
        inline fun build(filterName: String, filterValue: String, resultCount: Int, block: Builder.() -> Unit = {}) = Builder(filterName, filterValue, resultCount).apply(block).build()
    }

    class Builder(
        val filterName: String,
        val filterValue: String,
        val resultCount: Int
    ) {
        var items: List<TrackingItem>? = null
        var sectionName: String? = null
        var url: String = "Not Available"
        var analyticsTags: Map<String, String>? = null
        var resultId: String? = null
        var resultPage: Int? = null
        var resultOffset: Int? = null
        var sortOrder: String? = null
        var sortBy: String? = null
        var selectedFilters: Map<String, List<String>>? = null

        fun setItems(items: List<TrackingItem>): Builder = apply { this.items = items }
        fun setSectionName(sectionName: String): Builder = apply { this.sectionName = sectionName }
        fun setUrl(url: String): Builder = apply { this.url = url }
        fun setAnalyticsTags(analyticsTags: Map<String, String>): Builder = apply { this.analyticsTags = analyticsTags }
        fun setResultId(resultId: String): Builder = apply { this.resultId = resultId }
        fun setResultPage(resultPage: Int): Builder = apply { this.resultPage = resultPage }
        fun setResultOffset(resultOffset: Int): Builder = apply { this.resultOffset = resultOffset }
        fun setSortOrder(sortOrder: String): Builder = apply { this.sortOrder = sortOrder }
        fun setSortBy(sortBy: String): Builder = apply { this.sortBy = sortBy }
        fun setSelectedFilters(selectedFilters: Map<String, List<String>>): Builder = apply { this.selectedFilters = selectedFilters }
        fun build(): BrowseResultsLoadedData = BrowseResultsLoadedData(this)
    }
}
