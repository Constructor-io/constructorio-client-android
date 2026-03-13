package io.constructor.data.model.tracking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class MediaImpressionClickRequestBody(
        @Json(name = "banner_ad_id") val bannerAdId: String,
        @Json(name = "placement_id") val placementId: String,
        @Json(name = "beacon") val beacon: Boolean = true,
        @Json(name = "c") val c: String,
        @Json(name = "i") val i: String,
        @Json(name = "s") val s: Int,
        @Json(name = "key") val key: String,
        @Json(name = "ui") val ui: String?,
        @Json(name = "us") val us: List<String?>,
        @Json(name = "_dt") val dt: Long
) : Serializable
