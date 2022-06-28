package io.constructor.data.builder

import io.constructor.data.model.common.VariationsMap

/**
 * Create a Search request object utilizing a builder
 */
class SearchRequest (
    val term: String,
    val filters: Map<String, List<String>>? = null,
    val page: Int? = null,
    val perPage: Int? = null,
    val sortBy: String? = null,
    val sortOrder: String? = null,
    val section: String? = null,
    val hiddenFields: List<String>? = null,
    val hiddenFacets: List<String>? = null,
    val groupsSortBy: String? = null,
    val groupsSortOrder: String? = null,
    val variationsMap: VariationsMap? = null,
) {
    private constructor(builder: Builder) : this(
        builder.term,
        builder.filters,
        builder.page,
        builder.perPage,
        builder.sortBy,
        builder.sortOrder,
        builder.section,
        builder.hiddenFields,
        builder.hiddenFacets,
        builder.groupsSortBy,
        builder.groupsSortOrder,
        builder.variationsMap,
    )

    companion object {
        inline fun build(term: String, block: Builder.() -> Unit = {}) = Builder(term).apply(block).build()
    }

    class Builder(
        val term: String
    ) {
        var filters: Map<String, List<String>>? = null
        var page: Int? = null
        var perPage: Int? = null
        var sortBy: String? = null
        var sortOrder: String? = null
        var section: String? = null
        var hiddenFields: List<String>? = null
        var hiddenFacets: List<String>? = null
        var groupsSortBy: String? = null
        var groupsSortOrder: String? = null
        var variationsMap: VariationsMap? = null


        fun setFilters(facets: Map<String, List<String>>): Builder = apply { this.filters = facets }
        fun setPage(page: Int): Builder = apply { this.page = page }
        fun setPerPage(perPage: Int): Builder = apply { this.perPage = perPage }
        fun setSortBy(sortBy: String): Builder = apply { this.sortBy = sortBy }
        fun setSortOrder(sortOrder: String): Builder = apply { this.sortOrder = sortOrder }
        fun setSection(section: String): Builder = apply { this.section = section }
        fun setHiddenFields(hiddenFields: List<String>): Builder = apply { this.hiddenFields = hiddenFields }
        fun setHiddenFacets(hiddenFacets: List<String>): Builder = apply { this.hiddenFacets = hiddenFacets }
        fun setGroupsSortBy(groupsSortBy: String): Builder = apply { this.groupsSortBy = groupsSortBy }
        fun setGroupsSortOrder(groupsSortOrder: String): Builder = apply { this.groupsSortOrder = groupsSortOrder }
        fun setVariationsMap(variationsMap: VariationsMap): Builder = apply { this.variationsMap = variationsMap }
        fun build(): SearchRequest = SearchRequest(this)
    }
}