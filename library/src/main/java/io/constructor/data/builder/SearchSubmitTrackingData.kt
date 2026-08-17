package io.constructor.data.builder

import io.constructor.data.model.common.ResultGroup

/**
 * Create a Search Submit tracking request object utilizing a builder
 */
class SearchSubmitData(
    val searchTerm: String,
    val originalQuery: String,
    val resultGroup: ResultGroup? = null,
    val analyticsTags: Map<String, String>? = null,
) {
    private constructor(builder: Builder) : this(
        builder.searchTerm,
        builder.originalQuery,
        builder.resultGroup,
        builder.analyticsTags,
    )

    companion object {
        inline fun build(
            searchTerm: String,
            originalQuery: String,
            block: Builder.() -> Unit = {}
        ) = Builder(searchTerm, originalQuery).apply(block).build()
    }

    class Builder(
        val searchTerm: String,
        val originalQuery: String
    ) {
        var resultGroup: ResultGroup? = null
        var analyticsTags: Map<String, String>? = null

        fun setResultGroup(resultGroup: ResultGroup): Builder = apply { this.resultGroup = resultGroup }
        fun setAnalyticsTags(analyticsTags: Map<String, String>): Builder = apply { this.analyticsTags = analyticsTags }
        fun build(): SearchSubmitData = SearchSubmitData(this)
    }
}
