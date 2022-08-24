package io.constructor.util

import com.squareup.moshi.Moshi
import io.constructor.data.model.autocomplete.AutocompleteResponse
import io.constructor.data.model.search.SearchResponse
import okio.Buffer
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.charset.Charset


object TestDataLoader {

    fun loadResponse() : AutocompleteResponse? = loadResult("autocomplete_response.json")

    fun loadEmptyResponse() : AutocompleteResponse? = loadResult("autocomplete_response_empty.json")

    private fun loadResult(fileName: String): AutocompleteResponse? {
        val file = File(TestDataLoader::class.java.classLoader.getResource(fileName).path)
        val moshi = Moshi.Builder().build()
        val jsonAdapter = moshi.adapter(AutocompleteResponse::class.java)
        var response: AutocompleteResponse? = null
        try {
            response = jsonAdapter.fromJson(Buffer().readFrom(FileInputStream(file)))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return response
    }

    private fun convertToSearchResult(stringResponse: String): SearchResponse? {
        val moshi = Moshi.Builder().build()
        val jsonAdapter = moshi.adapter(SearchResponse::class.java)
        var result: SearchResponse? = null
        try {
            result = jsonAdapter.fromJson(stringResponse)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }

    fun loadAsString(fileName: String): String {
        var result = ""
        try {
            result = File(TestDataLoader::class.java.classLoader.getResource(fileName).path).inputStream().readBytes().toString(Charset.defaultCharset())
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }


}
