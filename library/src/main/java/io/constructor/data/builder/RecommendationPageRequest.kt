package io.constructor.data.builder

import io.constructor.data.model.common.VariationsMap

/**
 * Values that can be set per pod on a recommendation page request. Each value replaces (is not
 * merged with) the page-wide value for that pod.
 */
data class RecommendationPagePodOverride(
    val numResults: Int? = null,
    val filters: Map<String, List<String>>? = null,
    val filterMatchTypes: Map<String, String>? = null,
    val preFilterExpression: String? = null,
    val fmtOptions: Map<String, Any>? = null,
    val hiddenFields: List<String>? = null,
    val variationsMap: VariationsMap? = null,
)

/**
 * Create a Recommendation Page request object utilizing a builder
 */
class RecommendationPageRequest (
    val pageId: String,
    val itemIds: List<String>? = null,
    val variationId: String? = null,
    val term: String? = null,
    val section: String? = null,
    val numResults: Int? = null,
    val filters: Map<String, List<String>>? = null,
    val filterMatchTypes: Map<String, String>? = null,
    val preFilterExpression: String? = null,
    val fmtOptions: Map<String, Any>? = null,
    val hiddenFields: List<String>? = null,
    val variationsMap: VariationsMap? = null,
    val podOverrides: Map<String, RecommendationPagePodOverride>? = null,
) {
    private constructor(builder: Builder) : this(
        builder.pageId,
        builder.itemIds,
        builder.variationId,
        builder.term,
        builder.section,
        builder.numResults,
        builder.filters,
        builder.filterMatchTypes,
        builder.preFilterExpression,
        builder.fmtOptions,
        builder.hiddenFields,
        builder.variationsMap,
        builder.podOverrides,
    )

    /** The page-wide values that a pod override can replace */
    val pageWideOverridable: RecommendationPagePodOverride
        get() = RecommendationPagePodOverride(numResults, filters, filterMatchTypes, preFilterExpression, fmtOptions, hiddenFields, variationsMap)

    companion object {
        inline fun build(pageId: String, block: Builder.() -> Unit = {}) = Builder(pageId).apply(block).build()
    }

    class Builder(
        val pageId: String
    ) {
        var itemIds: List<String>? = null
        var variationId: String? = null
        var term: String? = null
        var section: String? = null
        var numResults: Int? = null
        var filters: Map<String, List<String>>? = null
        var filterMatchTypes: Map<String, String>? = null
        var preFilterExpression: String? = null
        var fmtOptions: Map<String, Any>? = null
        var hiddenFields: List<String>? = null
        var variationsMap: VariationsMap? = null
        var podOverrides: Map<String, RecommendationPagePodOverride>? = null

        fun setItemIds(itemIds: List<String>): Builder = apply { this.itemIds = itemIds }
        fun setVariationId(variationId: String): Builder = apply { this.variationId = variationId }
        fun setTerm(term: String): Builder = apply { this.term = term }
        fun setSection(section: String): Builder = apply { this.section = section }
        fun setNumResults(numResults: Int): Builder = apply { this.numResults = numResults }
        fun setFilters(filters: Map<String, List<String>>): Builder = apply { this.filters = filters }
        fun setFilterMatchTypes(filterMatchTypes: Map<String, String>): Builder = apply { this.filterMatchTypes = filterMatchTypes }
        fun setPreFilterExpression(preFilterExpression: String): Builder = apply { this.preFilterExpression = preFilterExpression }
        fun setFmtOptions(fmtOptions: Map<String, Any>): Builder = apply { this.fmtOptions = fmtOptions }
        fun setHiddenFields(hiddenFields: List<String>): Builder = apply { this.hiddenFields = hiddenFields }
        fun setVariationsMap(variationsMap: VariationsMap): Builder = apply { this.variationsMap = variationsMap }
        fun setPodOverrides(podOverrides: Map<String, RecommendationPagePodOverride>): Builder = apply { this.podOverrides = podOverrides }
        fun build(): RecommendationPageRequest = RecommendationPageRequest(this)
    }
}
