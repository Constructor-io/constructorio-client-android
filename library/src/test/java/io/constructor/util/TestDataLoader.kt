package io.constructor.util

import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import io.constructor.data.model.AutocompleteResponse
import io.constructor.data.model.search.SearchResponse
import okio.Buffer
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.charset.Charset


object TestDataLoader {

    fun loadResponse() : AutocompleteResponse? = loadResult<AutocompleteResponse>("response.json", AutocompleteResponse::class.java)

    fun loadSearchResponse() : SearchResponse? = loadResult<SearchResponse>("response.json", SearchResponse::class.java)

    fun loadResponseWithUnknownData() : AutocompleteResponse? = loadResult<AutocompleteResponse>("response_with_unexpected_data.json", AutocompleteResponse::class.java)

    fun loadEmptyResponse() : AutocompleteResponse? = loadResult<AutocompleteResponse>("empty_response.json", AutocompleteResponse::class.java)

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
