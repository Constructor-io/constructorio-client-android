package io.constructor.core

/**
 *  Encapsulates configuration of options related to displaying results
 *  @property page The page number of the results
 *  @property resultsPerPage The number of results per page
 *  @property sortBy The sort method of the results
 *  @property sortOrder The sort order for results
 *  @property section The section the results will come from
 */
data class ResultsConfig (
        val page: Int? = 1,
        val resultsPerPage: Int? = 24,
        val sortBy: String? = "relevance",
        val sortOrder: String? = "descending",
        val section: String? = "Products",
)
