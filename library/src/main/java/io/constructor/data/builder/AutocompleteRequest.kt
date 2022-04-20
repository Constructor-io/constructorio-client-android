package io.constructor.data.builder

/**
 * Create an Autocomplete request object utilizing a builder
 */
class AutocompleteRequest (
    val term: String,
    val filters: Map<String, List<String>>? = null,
    val numResultsPerSection: Map<String, Int>? = null,
    val hiddenFields: List<String>? = null,
) {
    private constructor(builder: Builder) : this(builder.term, builder.filters, builder.numResultsPerSection, builder.hiddenFields)

    companion object {
        inline fun build(term: String, block: Builder.() -> Unit = {}) = Builder(term).apply(block).build()
    }

    class Builder(
        val term: String
    ) {
        var filters: Map<String, List<String>>? = null
        var numResultsPerSection: Map<String, Int>? = null
        var hiddenFields: List<String>? = null

        fun setFilters(facets: Map<String, List<String>>): Builder = apply { this.filters = facets }
        fun setNumResultsPerSection(numResultsPerSection: Map<String, Int>): Builder = apply { this.numResultsPerSection = numResultsPerSection }
        fun setHiddenFields(hiddenFields: List<String>): Builder = apply { this.hiddenFields = hiddenFields }
        fun build(): AutocompleteRequest = AutocompleteRequest(this)
    }
}