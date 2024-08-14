package io.constructor.data.builder

import io.constructor.data.model.common.VariationsMap

/**
 * Create a Recommendations request object utilizing a builder
 */
class RecommendationsRequest (
    val podId: String,
    val filters: Map<String, List<String>>? = null,
    val itemIds: List<String>? = null,
    val term: String? = null,
    val numResults: Int? = null,
    val section: String? = null,
    val hiddenFields: List<String>? = null,
    val variationsMap: VariationsMap? = null,
    val preFilterExpression: String? = null,
) {
    private constructor(builder: Builder) : this(
        builder.podId,
        builder.filters,
        builder.itemIds,
        builder.term,
        builder.numResults,
        builder.section,
        builder.hiddenFields,
        builder.variationsMap,
        builder.preFilterExpression,
    )

    companion object {
        inline fun build(term: String, block: Builder.() -> Unit = {}) = Builder(term).apply(block).build()
    }

    class Builder(
        val podId: String
    ) {
        var filters: Map<String, List<String>>? = null
        var numResults: Int? = null
        var itemIds: List<String>? = null
        var term: String? = null
        var section: String? = null
        var hiddenFields: List<String>? = null
        var variationsMap: VariationsMap? = null
        var preFilterExpression: String? = null

        fun setFilters(facets: Map<String, List<String>>): Builder = apply { this.filters = facets }
        fun setItemIds(itemIds: List<String>): Builder = apply { this.itemIds = itemIds }
        fun setTerm(term: String): Builder = apply { this.term = term }
        fun setNumResults(numResults: Int): Builder = apply { this.numResults = numResults }
        fun setSection(section: String): Builder = apply { this.section = section }
        fun setHiddenFields(hiddenFields: List<String>): Builder = apply { this.hiddenFields = hiddenFields }
        fun setVariationsMap(variationsMap: VariationsMap): Builder = apply { this.variationsMap = variationsMap }
        fun setPreFilterExpression(preFilterExpression: String): Builder = apply { this.preFilterExpression = preFilterExpression }
        fun build(): RecommendationsRequest = RecommendationsRequest(this)
    }
}