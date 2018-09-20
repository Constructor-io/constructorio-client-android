package io.constructor.data.remote

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import io.constructor.data.model.ResultGroup
import io.constructor.data.model.search.ResultData
import io.constructor.data.model.search.ResultFacet

class ResultDataAdapter {

    @FromJson
    fun fromJson(jsonReader: JsonReader, facetAdapter: JsonAdapter<List<ResultFacet>>, groupAdapter: JsonAdapter<List<ResultGroup>>) : ResultData {
        val properties = mutableMapOf<String, String>()
        var id = ""
        var description = ""
        var imageUrl = ""
        var url = ""
        var facets: List<ResultFacet>? = null
        var groups: List<ResultGroup>? = null
        jsonReader.beginObject()
        while (jsonReader.hasNext()) {
            val propName = jsonReader.nextName()
            when (propName) {
                "id" -> id = jsonReader.nextString()
                "description" -> description = jsonReader.nextString()
                "image_url" -> imageUrl = jsonReader.nextString()
                "url" -> url = jsonReader.nextString()
                "facets" -> facets = facetAdapter.fromJson(jsonReader)
                "groups" -> groups = groupAdapter.fromJson(jsonReader)
                else -> properties[propName] = jsonReader.nextString()
            }
        }
        jsonReader.endObject()
        return ResultData(description, id, imageUrl, url, facets, groups, properties)
    }
}