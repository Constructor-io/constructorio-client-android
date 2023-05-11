package io.constructor.data.builder

/**
 * Create a Browse facets request object utilizing a builder
 */
class BrowseFacetsRequest (
        val page: Int? = null,
        val offset: Int? = null,
        val numResultsPerPage: Int? = null,
        val showHiddenFacets: Boolean? = null,
) {
    private constructor(builder: Builder) : this(
            builder.page,
            builder.offset,
            builder.numResultsPerPage,
            builder.showHiddenFacets,
    )

    companion object {
        inline fun build(block: BrowseFacetsRequest.Builder.() -> Unit = {}) = BrowseFacetsRequest.Builder().apply(block).build()
    }

    class Builder(
    ) {
        var page: Int? = null
        var offset: Int? = null
        var numResultsPerPage: Int? = null
        var showHiddenFacets: Boolean? = null

        fun setPage(page: Int): Builder = apply { this.page = page }
        fun setOffset(offset: Int): Builder = apply { this.offset = offset }
        fun setNumResultsPerPage(numResultsPerPage: Int): Builder = apply { this.numResultsPerPage = numResultsPerPage }
        fun setShowHiddenFacets(showHiddenFacets: Boolean): Builder = apply { this.showHiddenFacets = showHiddenFacets }
        fun build(): BrowseFacetsRequest = BrowseFacetsRequest(this)
    }
}