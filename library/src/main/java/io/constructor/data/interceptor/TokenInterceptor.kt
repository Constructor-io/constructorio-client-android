package io.constructor.data.interceptor

import android.content.Context
import io.constructor.core.Constants
import io.constructor.data.local.PreferencesHelper
import okhttp3.Interceptor
import okhttp3.Response



class TokenInterceptor(val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = PreferencesHelper(context).getToken()
        var request = chain.request()
        val url = request.url().newBuilder().addQueryParameter(Constants.QueryConstants.AUTOCOMPLETE_KEY, token).build()
        request = request.newBuilder().url(url).build()
        return chain.proceed(request)
    }
}