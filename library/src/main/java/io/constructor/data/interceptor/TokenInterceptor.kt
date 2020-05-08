package io.constructor.data.interceptor

import android.content.Context
import io.constructor.BuildConfig
import io.constructor.core.Constants
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.util.urlEncode
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds common request query parameters to all API requests
 */
class TokenInterceptor(val context: Context, private val preferencesHelper: PreferencesHelper, private val configMemoryHolder: ConfigMemoryHolder) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.url().newBuilder()
            .addQueryParameter(Constants.QueryConstants.API_KEY, preferencesHelper.token)
            .addQueryParameter(Constants.QueryConstants.IDENTITY, preferencesHelper.id)

        // TODO : Urlencode
        configMemoryHolder.userId?.let {
            builder.addQueryParameter(Constants.QueryConstants.USER_ID, it)
        }

        builder.addQueryParameter(Constants.QueryConstants.SESSION, preferencesHelper.getSessionId().toString())

        // TODO : Urlencode
        configMemoryHolder.testCellParams.forEach {
            it?.let {
                builder.addQueryParameter(it.first, it.second)
            }
        }

        builder.addQueryParameter(Constants.QueryConstants.CLIENT, BuildConfig.CLIENT_VERSION)
        builder.addQueryParameter(Constants.QueryConstants.TIMESTAMP, System.currentTimeMillis().toString())

        val url = builder.build()
        val newRequest = request.newBuilder().url(url).build()
        return chain.proceed(newRequest)
    }
}