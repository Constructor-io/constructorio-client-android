package io.constructor.data.interceptor

import io.constructor.BuildConfig
import io.constructor.core.Constants
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.remote.ApiPaths
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response

/**
 * @suppress
 * Adds common request query parameters to all API requests
 */
class RequestInterceptor(
    private val preferencesHelper: PreferencesHelper,
    private val configMemoryHolder: ConfigMemoryHolder
) : Interceptor {
    private fun redactPii(query: String): String {
        val emailRegex = Regex("[\\w\\-+\\\\.]+@([\\w-]+\\.)+[\\w-]{2,4}")
        val phoneRegex = Regex("^(?:\\+\\d{11,12}|\\+\\d{1,3}\\s\\d{3}\\s\\d{3}\\s\\d{3,4}|\\(\\d{3}\\)\\d{7}|\\(\\d{3}\\)\\s\\d{3}\\s\\d{4}|\\(\\d{3}\\)\\d{3}-\\d{4}|\\(\\d{3}\\)\\s\\d{3}-\\d{4})\$")
        val creditCardRegex = Regex("^(?:4[0-9]{15}|(?:5[1-5][0-9]{2}|222[1-9]|22[3-9][0-9]|2[3-6][0-9]{2}|27[01][0-9]|2720)[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|6(?:011|5[0-9]{2})[0-9]{12}|(?:2131|1800|35\\d{3})\\d{11})\$")

        if (query.let { emailRegex.containsMatchIn(it) }) {
            return emailRegex.replace(query, "<email_omitted>")
        }

        if (query.let { phoneRegex.containsMatchIn(it) }) {
            return phoneRegex.replace(query, "<phone_omitted>")
        }

        if (query.let { creditCardRegex.containsMatchIn(it) }) {
            return creditCardRegex.replace(query, "<credit_omitted>")
        }
        return query
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val ignoreDtPaths = listOf(ApiPaths.URL_BROWSE_GROUPS, ApiPaths.URL_BROWSE_FACETS, ApiPaths.URL_BROWSE_FACET_OPTIONS);
        val behavioralEndpointPaths = listOf(ApiPaths.URL_BEHAVIORAL_V1_PREFIX.toRegex(), ApiPaths.URL_BEHAVIORAL_V2_PREFIX.toRegex(), ApiPaths.URL_BEHAVIORAL_SEARCH_REGEX.toRegex() )
        val request = chain.request()
        var builder = request.url.newBuilder();
        val newRequestBuilder = request.newBuilder()

        /* Re-add, Redact url query parameters for /behavior, /v2/behavioral_action */
        if (behavioralEndpointPaths.any{request.url.encodedPath.matches(it)} ) {
            builder = HttpUrl.Builder()
                    .scheme(request.url.scheme)
                    .port(request.url.port)
                    .host(request.url.host)
                    .encodedPath(request.url.encodedPath);
            request.url.queryParameterNames.forEach{
                name ->
                request.url.queryParameterValues(name).forEach{
                    paramValue ->
                    if (paramValue is String) {
                        builder.addQueryParameter(name, redactPii(paramValue))
                    }
                }
            }
        }

        /* Add Session and Identity Parameters */
        builder.port(preferencesHelper.port)
            .addQueryParameter(Constants.QueryConstants.API_KEY, preferencesHelper.apiKey)
            .addQueryParameter(Constants.QueryConstants.IDENTITY, preferencesHelper.id)
        configMemoryHolder.userId?.let {
            builder.addQueryParameter(Constants.QueryConstants.USER_ID, it)
        }
        builder.addQueryParameter(Constants.QueryConstants.SESSION, preferencesHelper.getSessionId().toString())
        configMemoryHolder.testCellParams.forEach {
            it?.let {
                builder.addQueryParameter("ef-" + it.first, it.second)
            }
        }
        configMemoryHolder.segments.forEach {
            it?.let {
                builder.addQueryParameter(Constants.QueryConstants.SEGMENTS, it)
            }
        }
        builder.addQueryParameter(Constants.QueryConstants.CLIENT, BuildConfig.CLIENT_VERSION)

        /* Add Timestamp Parameter */
        if (ignoreDtPaths.none { path -> request.url.encodedPath.endsWith(path)}) {
            builder.addQueryParameter(Constants.QueryConstants.TIMESTAMP, System.currentTimeMillis().toString())
        }

        val newRequest = newRequestBuilder.url(builder.build()).build();
        return chain.proceed(newRequest)
    }
}