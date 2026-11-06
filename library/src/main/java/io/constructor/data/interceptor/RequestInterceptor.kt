package io.constructor.data.interceptor

import io.constructor.BuildConfig
import io.constructor.core.Constants
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.remote.ApiPaths
import io.constructor.util.redactPii
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
    private fun redactPathSegments(pathSegments: List<String>): String {
        var redactedPath = pathSegments.map {
            it.redactPii()
        }

        return redactedPath.joinToString("/")
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val ignoreDtPaths = listOf(ApiPaths.URL_BROWSE_GROUPS, ApiPaths.URL_BROWSE_FACETS, ApiPaths.URL_BROWSE_FACET_OPTIONS);
        val behavioralEndpointPrefixes = listOf(ApiPaths.URL_BEHAVIORAL_V1_PREFIX, ApiPaths.URL_BEHAVIORAL_V2_PREFIX, ApiPaths.URL_BEHAVIORAL_AD_PREFIX)
        val behavioralSearchRegex = ApiPaths.URL_BEHAVIORAL_SEARCH_REGEX.toRegex()
        val request = chain.request()
        var builder = request.url.newBuilder();
        val newRequestBuilder = request.newBuilder()

        /* Re-add, Redact url query parameters for /behavior, /v2/behavioral_action */
        val encodedPath = request.url.encodedPath
        val isBehavioralEndpoint = behavioralEndpointPrefixes.any { encodedPath.startsWith(it) } || behavioralSearchRegex.matches(encodedPath)
        if (isBehavioralEndpoint) {
            builder = HttpUrl.Builder()
                    .scheme(request.url.scheme)
                    .port(request.url.port)
                    .host(request.url.host)
                    .addPathSegments(redactPathSegments(request.url.pathSegments));

            request.url.queryParameterNames.forEach{
                name ->
                request.url.queryParameterValues(name).forEach{
                    paramValue ->
                    if (paramValue is String) {
                        builder.addQueryParameter(name, paramValue.redactPii())
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