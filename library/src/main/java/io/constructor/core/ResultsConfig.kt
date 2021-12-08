package io.constructor.core

/**
 *  Encapsulates configuration for the result display options
 *  @property page The page number of the results
 *  @property resultsPerPage The number of results per page
 *  @property sortBy The sort method of the results
 *  @property sortOrder The sort order for results
 *  @property section The section the results will come from
 */
data class ResultsConfig (
        val page: Int = 1,
        val resultsPerPage: Int = 20,
        val sortBy: String = "relevance",
        val sortOrder: String = "descending",
        val section: String = "Products",
)