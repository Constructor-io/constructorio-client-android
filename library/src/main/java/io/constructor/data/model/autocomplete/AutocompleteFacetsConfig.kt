package io.constructor.data.model.autocomplete

/**
 *  Encapsulates configuration of the options related to facets
 *  @property facets The filters used to refine results
 *  @property fmtOptions The format options used to refine result groups
 *  @property hiddenFields Hidden metadata fields to return
 */

data class AutocompleteFacetsConfig (
            val facets: Map<String, List<String>>? = null,
            val fmtOptions: Map<String, String>? = null,
            val hiddenFields: List<String>? = null,
)
