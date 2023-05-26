package io.constructor.data.builder

import org.junit.Test
import kotlin.test.assertEquals

class BrowseGroupsRequestTest {
    private val groupId = "Brand"
    private val groupsMaxDepth = 5

    @Test
    fun browseGroupsRequestWithGroupIdUsingBuilder() {
        val request = BrowseGroupsRequest.Builder()
                .setGroupId(groupId)
                .build()
        assertEquals(request.groupId, groupId)
    }

    @Test
    fun browseGroupsRequestWithGroupsMaxDepthUsingBuilder() {
        val request = BrowseGroupsRequest.Builder()
                .setGroupsMaxDepth(groupsMaxDepth)
                .build()
        assertEquals(request.groupsMaxDepth, groupsMaxDepth)
    }

    @Test
    fun browseGroupsRequestWithParamsUsingDSL() {
        val request = BrowseGroupsRequest.build() {
            groupId = this@BrowseGroupsRequestTest.groupId
            groupsMaxDepth = this@BrowseGroupsRequestTest.groupsMaxDepth
        }

        assertEquals(request.groupsMaxDepth, groupsMaxDepth)
        assertEquals(request.groupId, groupId)
    }
}
