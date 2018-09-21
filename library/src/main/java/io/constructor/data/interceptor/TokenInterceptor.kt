package io.constructor.data.interceptor

import android.content.Context
import io.constructor.BuildConfig
import io.constructor.core.Constants
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.TestCellMemoryHolder
import okhttp3.Interceptor
import okhttp3.Response


class TokenInterceptor(val context: Context, private val preferencesHelper: PreferencesHelper, private val testCellMemoryHolder: TestCellMemoryHolder) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val builder = request.url().newBuilder()
                .addQueryParameter(Constants.QueryConstants.AUTOCOMPLETE_KEY, preferencesHelper.token)
                .addQueryParameter(Constants.QueryConstants.IDENTITY, preferencesHelper.id)
                .addQueryParameter(Constants.QueryConstants.TIMESTAMP, System.currentTimeMillis().toString())
                .addQueryParameter(Constants.QueryConstants.CLIENT, BuildConfig.CLIENT_VERSION)
        testCellMemoryHolder.testCellParams.forEach {
            it?.let {
                builder.addQueryParameter(it.first, it.second)
            }
        }
        val url = builder.build()
        request = request.newBuilder().url(url).build()
        return chain.proceed(request)
    }
}