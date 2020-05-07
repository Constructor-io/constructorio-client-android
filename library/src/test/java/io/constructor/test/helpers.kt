package io.constructor.test

import android.content.Context
import io.constructor.data.DataManager
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.remote.ConstructorApi
import io.constructor.injection.module.NetworkModule
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockWebServer
import java.util.concurrent.TimeUnit

/**
 * Creates a data manager that communicates with a Mock Web Server
 */
fun createTestDataManager(mockServer : MockWebServer,
                          preferencesHelper: PreferencesHelper,
                          configMemoryHolder: ConfigMemoryHolder,
                          ctx: Context
): DataManager {

    val basePath = mockServer.url("")
    val networkModule = NetworkModule(ctx);
    val loggingInterceptor = networkModule.provideHttpLoggingInterceptor()
    val tokenInterceptor = networkModule.provideTokenInterceptor(preferencesHelper, configMemoryHolder)
    val moshi = networkModule.provideMoshi()

    // Intercept all requests to the Constructor API and point them to a mock web server
    val okHttpClient = networkModule.provideOkHttpClient(loggingInterceptor, tokenInterceptor).newBuilder().addInterceptor { chain ->
        var request = chain.request()
        val requestUrl = request.url()
        val newRequestUrl = HttpUrl.Builder().scheme(basePath.scheme())
                .encodedQuery(requestUrl.encodedQuery())
                .host(basePath.host())
                .port(basePath.port())
                .encodedPath(requestUrl.encodedPath()).build()
        request = request.newBuilder()
                .url(newRequestUrl)
                .build()
        chain.proceed(request)
    }.readTimeout(1, TimeUnit.SECONDS).build()
    val retrofit = networkModule.provideRetrofit(okHttpClient, moshi)
    val constructorApi = retrofit.create(ConstructorApi::class.java)
    return DataManager(constructorApi, moshi);
}