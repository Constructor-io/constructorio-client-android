package io.constructor.data.model.search

/**
 *  Encapsulates configuration of the options related to facets
 *  @property facets The filters used to refine results
 *  @property fmtOptions The format options used to refine result groups
 *  @property hiddenFields Hidden metadata fields to return
 *  @property hiddenFacets Hidden facet fields to return
 */

data class SearchFacetsConfig (
        val facets: Map<String, List<String>>? = null,
        val fmtOptions: Map<String, String>? = null,
        val hiddenFields: List<String>? = null,
        val hiddenFacets: List<String>? = null,
        
)
