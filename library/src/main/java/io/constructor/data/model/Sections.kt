package io.constructor.data.model

import com.squareup.moshi.Json


class Sections {
    @Json(name="Search Suggestions")
    lateinit var suggestions: List<Suggestion>

    @Json(name="Products")
    lateinit var products: List<Product>
}