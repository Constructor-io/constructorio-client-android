package io.constructor.data.model.recommendations

/**
 *  Encapsulates configuration of the primary recommendation inputs
 *  @property podId The pod id specific to the recommendation
 *  @property itemId The item id to refine results (strategy/podId specific)
 *  @property term The term to use to refine results (strategy/podId specific)
 */
data class RecommendationConfig (
        val podId: String,
        val itemId: List<String>? = null,
        val term: String? = null,
)
