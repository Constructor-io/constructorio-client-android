package io.constructor.core

/**
 *  Encapsulates configuration of options related to displaying results
 *  @property resultsPerPage The number of results per page
 *  @property page The page number of the results
 *  @property sortBy The sort method of the results
 *  @property sortOrder The sort order for results
 *  @property section The section the results will come from
 */
data class ResultsConfig (
        val resultsPerPage: Int? = null,
        val page: Int? = null,
        val sortBy: String? = null,
        val sortOrder: String? = null,
        val section: String? = null,
)
