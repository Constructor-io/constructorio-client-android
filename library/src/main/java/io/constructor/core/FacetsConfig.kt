package io.constructor.core

/**
 *  Encapsulates configuration for the result display options
 *  @property facets The filters used to refine results
 *  @property fmtOptions The format options used to refine result groups
 *  @property hiddenFields Hidden metadata fields to return
 */
data class FacetsConfig (
        val facets: List<Pair<String, List<String>>>? = null,
        val fmtOptions: List<Pair<String, List<String>>>? = null,
        val hiddenFields: List<String>? = null,
)