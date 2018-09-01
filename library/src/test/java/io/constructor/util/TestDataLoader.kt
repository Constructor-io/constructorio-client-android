package io.constructor.util

import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import io.constructor.data.model.AutocompleteResult
import io.constructor.data.model.search.SearchResponse
import okio.Buffer
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.charset.Charset


object TestDataLoader {

    fun loadResponse() : AutocompleteResult? = loadResult<AutocompleteResult>("response.json", AutocompleteResult::class.java)

    fun loadSearchResponse() : SearchResponse? = loadResult<SearchResponse>("response.json", SearchResponse::class.java)

    fun loadResponseWithUnknownData() : AutocompleteResult? = loadResult<AutocompleteResult>("response_with_unexpected_data.json", AutocompleteResult::class.java)

    fun loadEmptyResponse() : AutocompleteResult? = loadResult<AutocompleteResult>("empty_response.json", AutocompleteResult::class.java)

    private fun <T> loadResult(fileName: String, responseClass: Class<*>): T? {
        val file = File(TestDataLoader::class.java.classLoader.getResource(fileName).path)
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter = moshi.adapter<T>(responseClass)
        var result: T? = null
        try {
            result = jsonAdapter.fromJson(Buffer().readFrom(FileInputStream(file)))
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
