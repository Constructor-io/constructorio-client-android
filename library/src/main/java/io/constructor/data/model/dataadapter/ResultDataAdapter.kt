package io.constructor.data.model.dataadapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import io.constructor.data.model.Group
import io.constructor.data.model.search.ResultData
import io.constructor.data.model.search.ResultFacet

class ResultDataAdapter {

    companion object {
        val NAMES = JsonReader.Options.of("id", "description", "image_url", "url", "facets", "groups")
    }

    @FromJson fun fromJson(jsonReader: JsonReader, facetDelegate: JsonAdapter<List<ResultFacet>>, groupDelegate: JsonAdapter<List<Group>>): ResultData {
        jsonReader.beginObject()
        var metadata: HashMap<String, Any?> = hashMapOf()
        var id = ""
        var description: String? = null
        var imageUrl: String? = null
        var url: String? = null
        var facets: List<ResultFacet>? = null
        var groups: List<Group>? = null
        while (jsonReader.hasNext()) {
            when (jsonReader.selectName(NAMES)) {
                0 -> {
                    id = jsonReader.nextString()
                }
                1 -> {
                    description = jsonReader.nextString()
                }
                2 -> {
                    imageUrl = jsonReader.nextString()
                }
                3 -> {
                    url = jsonReader.nextString()

                }
                4 -> {
                    facets = facetDelegate.fromJsonValue(jsonReader.readJsonValue())
                }
                5 -> {
                    groups = groupDelegate.fromJsonValue(jsonReader.readJsonValue())
                }
                else -> {
                    metadata[jsonReader.nextName()] = jsonReader.readJsonValue()
                }
            }
        }
        jsonReader.endObject()
        return ResultData(description, id, imageUrl, url, facets, groups, metadata)

    }

}