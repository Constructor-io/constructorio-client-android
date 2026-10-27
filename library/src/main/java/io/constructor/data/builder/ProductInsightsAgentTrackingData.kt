package io.constructor.data.builder

/**
 * The parameters shared by every Product Insights Agent tracking request object
 *
 * @property itemId the ID of the item (product) the event is for, i.e. "1234567-AB"
 * @property itemName the name of the item (product) the event is for, i.e. "Waterproof Jacket"
 * @property variationId the variation ID of the item (product) the event is for
 * @property threadId the ID of the Product Insights Agent conversation thread
 * @property sectionName the index section, i.e. "Products". Defaults to [io.constructor.core.ConstructorIoConfig.defaultItemSection]
 * @property analyticsTags additional analytics tags to pass. Merged with [io.constructor.core.ConstructorIoConfig.defaultAnalyticsTags], with request-level values overriding defaults on key collision
 */
interface ProductInsightsAgentTrackingData {
    val itemId: String
    val itemName: String?
    val variationId: String?
    val threadId: String?
    val sectionName: String?
    val analyticsTags: Map<String, String>?
}
