package io.constructor.data.builder

/**
 * Create a Browse groups request object utilizing a builder
 */
class BrowseGroupsRequest (
        val groupId: String? = null,
        val groupsMaxDepth: Int? = null,
) {
    private constructor(builder: Builder) : this(
            builder.groupId,
            builder.groupsMaxDepth,
    )

    companion object {
        inline fun build(block: BrowseGroupsRequest.Builder.() -> Unit = {}) = BrowseGroupsRequest.Builder().apply(block).build()
    }

    class Builder(
    ) {
        var groupId: String? = null
        var groupsMaxDepth: Int? = null

        fun setGroupId(groupId: String): Builder = apply { this.groupId = groupId }
        fun setGroupsMaxDepth(groupsMaxDepth: Int): Builder = apply { this.groupsMaxDepth = groupsMaxDepth }
        fun build(): BrowseGroupsRequest = BrowseGroupsRequest(this)
    }
}