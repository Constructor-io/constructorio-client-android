package io.constructor.core

import io.constructor.BuildConfig

/**
 *  Encapsulates configuration options for the Constructor SDK client
 *  @property apiKey Constructor.io API key
 *  @property serviceUrl Constructor.io service URL (defaults to "ac.cnstrc.com")
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
        val defaultItemSection: String = BuildConfig.DEFAULT_ITEM_SECTION,
        val servicePort: Int = BuildConfig.SERVICE_PORT,
        val serviceScheme: String = BuildConfig.SERVICE_SCHEME
)