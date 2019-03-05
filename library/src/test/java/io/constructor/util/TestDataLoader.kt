package io.constructor.util

import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import io.constructor.data.model.AutocompleteResult
import okio.Buffer
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.charset.Charset


object TestDataLoader {

    fun loadResponse() : AutocompleteResult? = loadResult("response.json")

    fun loadResponseWithUnexpectedData() : AutocompleteResult? = loadResult("response_with_unexpected_data.json")

    fun loadEmptyResponse() : AutocompleteResult? = loadResult("empty_response.json")

    private fun loadResult(fileName: String): AutocompleteResult? {
        val file = File(TestDataLoader::class.java.classLoader.getResource(fileName).path)
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter = moshi.adapter(AutocompleteResult::class.java)
        var result: AutocompleteResult? = null
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
