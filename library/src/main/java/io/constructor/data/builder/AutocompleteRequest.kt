package io.constructor.data.builder

import io.constructor.data.model.common.VariationsMap

/**
 * Create an Autocomplete request object utilizing a builder
 */
class AutocompleteRequest (
    val term: String,
    val filters: Map<String, List<String>>? = null,
    val numResultsPerSection: Map<String, Int>? = null,
    val hiddenFields: List<String>? = null,
    val variationsMap: VariationsMap? = null,
    val sectionFilters: Map<String, Map<String, List<String>>>? = null
) {
    private constructor(builder: Builder) : this(
        builder.term,
        builder.filters,
        builder.numResultsPerSection,
        builder.hiddenFields,
        builder.variationsMap,
        builder.sectionFilters
    )

    companion object {
        inline fun build(term: String, block: Builder.() -> Unit = {}) = Builder(term).apply(block).build()
    }

    class Builder(
        val term: String
    ) {
        var filters: Map<String, List<String>>? = null
        var numResultsPerSection: Map<String, Int>? = null
        var hiddenFields: List<String>? = null
        var variationsMap: VariationsMap? = null
        var sectionFilters: Map<String, Map<String, List<String>>>? = null

        fun setFilters(facets: Map<String, List<String>>): Builder = apply { this.filters = facets }
        fun setNumResultsPerSection(numResultsPerSection: Map<String, Int>): Builder = apply { this.numResultsPerSection = numResultsPerSection }
        fun setHiddenFields(hiddenFields: List<String>): Builder = apply { this.hiddenFields = hiddenFields }
        fun setVariationsMap(variationsMap: VariationsMap): Builder = apply { this.variationsMap = variationsMap }
        fun setSectionFilters(sectionFilters: Map<String, Map<String, List<String>>>?): Builder = apply { this.sectionFilters = sectionFilters }
        fun build(): AutocompleteRequest = AutocompleteRequest(this)
    }
}