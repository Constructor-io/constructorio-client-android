package io.constructor.data.model.recommendations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Recommendation page response. [resultId] identifies the page request and is not a tracking id:
 * send each pod's own [RecommendationPagePod.resultId] with that pod's tracking events.
 */
@JsonClass(generateAdapter = true)
data class RecommendationPageResponse(
        @Json(name = "response") val response: RecommendationPageResponseInner?,
        @Json(name = "result_id") val resultId: String?,
        @Json(name = "request") val request: Map<String, Any?>?,
        var rawData: String?
) : Serializable

@JsonClass(generateAdapter = true)
data class RecommendationPageResponseInner(
        @Json(name = "page_id") val pageId: String?,
        @Json(name = "display_name") val displayName: String?,
        @Json(name = "page_type") val pageType: String?,
        @Json(name = "pods") val pods: List<RecommendationPagePod>?
) : Serializable

/**
 * One pod of a recommendation page. [response] has the same shape as a single-pod response, and
 * [resultId] is the id to send with this pod's recommendation view and click events.
 */
@JsonClass(generateAdapter = true)
data class RecommendationPagePod(
        @Json(name = "pod_id") val podId: String,
        @Json(name = "request") val request: Map<String, Any?>?,
        @Json(name = "response") val response: RecommendationsResponseInner?,
        @Json(name = "result_id") val resultId: String?
) : Serializable {
    /**
     * This pod as a single-pod [RecommendationsResponse], carrying the pod's own result id, so
     * code that renders and tracks `getRecommendationResults` responses can be reused.
     */
    fun toRecommendationsResponse(): RecommendationsResponse =
            RecommendationsResponse(response, resultId, (request ?: emptyMap()) + ("pod_id" to podId), null)
}
