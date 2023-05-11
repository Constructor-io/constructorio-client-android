package io.constructor.data.builder

/**
 * Create a Browse facet options request object utilizing a builder
 */
class BrowseFacetOptionsRequest (
        val facetName: String,
        val showHiddenFacets: Boolean? = null,
) {
    private constructor(builder: Builder) : this(
            builder.facetName,
            builder.showHiddenFacets,
    )

    companion object {
        inline fun build(facetName: String, block: BrowseFacetOptionsRequest.Builder.() -> Unit = {}) = BrowseFacetOptionsRequest.Builder(facetName).apply(block).build()
    }

    class Builder(
        val facetName: String,
    ) {
        var showHiddenFacets: Boolean? = null

        fun setShowHiddenFacets(showHiddenFacets: Boolean): Builder = apply { this.showHiddenFacets = showHiddenFacets }
        fun build():  BrowseFacetOptionsRequest = BrowseFacetOptionsRequest(this)
    }
}