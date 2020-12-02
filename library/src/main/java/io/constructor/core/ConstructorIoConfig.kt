package io.constructor.core

import io.constructor.BuildConfig

/**
 *  Used to configure the Constructor SDK client
 *  @property apiKey Constructor.io API key
 *  @property serviceUrl Constructor.io service URL (defaults to "ac.cnstrc.com")
 *  @property autocompleteResultCount The number of results to return per section when requesting autocomplete results
 *  @property defaultItemSection The item section when requesting data and sending tracking events (defaults to "Products")
 *  @property segments Arbitrary customer defined user segments
 *  @property testCells Test cell name/value pairs if A/B testing
 *  @property servicePort The port to use (for testing purposes only, defaults to 443)
 *  @property serviceScheme The scheme to use (for testing purposes only, defaults to HTTPS)
 */
data class ConstructorIoConfig(
        val apiKey: String,
        val serviceUrl: String = BuildConfig.SERVICE_URL,
        val segments: List<String> = emptyList(),
        val testCells: List<Pair<String, String>> = emptyList(),
        val autocompleteResultCount: Map<String, Int> = mapOf(Constants.QueryValues.SEARCH_SUGGESTIONS to 10, Constants.QueryValues.PRODUCTS to 0),
        val defaultItemSection: String = BuildConfig.DEFAULT_ITEM_SECTION,
        val servicePort: Int = BuildConfig.SERVICE_PORT,
        val serviceScheme: String = BuildConfig.SERVICE_SCHEME
)