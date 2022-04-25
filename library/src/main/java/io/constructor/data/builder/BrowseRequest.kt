package io.constructor.data.builder

/**
 * Create a Browse request object utilizing a builder
 */
class BrowseRequest (
    val filterName: String,
    val filterValue: String,
    val filters: Map<String, List<String>>? = null,
    val page: Int? = null,
    val perPage: Int? = null,
    val sortBy: String? = null,
    val sortOrder: String? = null,
    val section: String? = null,
    val hiddenFields: List<String>? = null,
    val hiddenFacets: List<String>? = null,
) {
    private constructor(builder: Builder) : this(
        builder.filterName,
        builder.filterValue,
        builder.filters,
        builder.page,
        builder.perPage,
        builder.sortBy,
        builder.sortOrder,
        builder.section,
        builder.hiddenFields,
        builder.hiddenFacets,
    )

    companion object {
        inline fun build(filterName: String, filterValue: String, block: Builder.() -> Unit = {}) = Builder(filterName, filterValue).apply(block).build()
    }

    class Builder(
        val filterName: String,
        val filterValue: String
    ) {
        var filters: Map<String, List<String>>? = null
        var page: Int? = null
        var perPage: Int? = null
        var sortBy: String? = null
        var sortOrder: String? = null
        var section: String? = null
        var hiddenFields: List<String>? = null
        var hiddenFacets: List<String>? = null

        fun setFilters(facets: Map<String, List<String>>): Builder = apply { this.filters = facets }
        fun setPage(page: Int): Builder = apply { this.page = page }
        fun setPerPage(perPage: Int): Builder = apply { this.perPage = perPage }
        fun setSortBy(sortBy: String): Builder = apply { this.sortBy = sortBy }
        fun setSortOrder(sortOrder: String): Builder = apply { this.sortOrder = sortOrder }
        fun setSection(section: String): Builder = apply { this.section = section }
        fun setHiddenFields(hiddenFields: List<String>): Builder = apply { this.hiddenFields = hiddenFields }
        fun setHiddenFacets(hiddenFacets: List<String>): Builder = apply { this.hiddenFacets = hiddenFacets }
        fun build(): BrowseRequest = BrowseRequest(this)
    }
}