package io.constructor.data.model.autocomplete

/**
 *  Encapsulates configuration of options related to displaying results
 *  @property numResultsPerSection The number of results per section
 */
data class AutocompleteResultsConfig (
        val numResultsPerSection: Map<String, Int>? = null,
)
