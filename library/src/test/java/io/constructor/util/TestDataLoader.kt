package io.constructor.util

import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import io.constructor.data.model.Result
import okio.Buffer
import java.io.File
import java.io.FileInputStream
import java.io.IOException


object TestDataLoader {

    fun loadResponse() : Result? = loadResult("response.json")

    fun loadResponseWithUnknownData() : Result? = loadResult("response_with_unexpected_data.json")

    fun loadEmptyResponse() : Result? = loadResult("empty_response.json")

    private fun loadResult(fileName: String): Result? {
        val file = File(TestDataLoader::class.java.classLoader.getResource(fileName).path)
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter = moshi.adapter(Result::class.java)
        var result: Result? = null
        try {
            result = jsonAdapter.fromJson(Buffer().readFrom(FileInputStream(file)))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }

}
