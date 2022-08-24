package io.constructor.test

import io.constructor.data.DataManager
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.remote.ConstructorApi
import io.constructor.injection.module.NetworkModule
import java.util.concurrent.TimeUnit

/**
 * Creates a data manager that communicates with a Mock Web Server
 */
fun createTestDataManager(
    preferencesHelper: PreferencesHelper,
    configMemoryHolder: ConfigMemoryHolder
): DataManager {
    val loggingInterceptor = NetworkModule.provideHttpLoggingInterceptor()
    val requestInterceptor = NetworkModule.provideRequestInterceptor(preferencesHelper, configMemoryHolder)
    val moshi = NetworkModule.provideMoshi()
    val okHttpClient = NetworkModule.provideOkHttpClient(loggingInterceptor, requestInterceptor).newBuilder().addInterceptor { chain ->
        var request = chain.request()
        chain.proceed(request)
    }.readTimeout(1, TimeUnit.SECONDS).build()
    val retrofit = NetworkModule.provideRetrofit(okHttpClient, moshi, preferencesHelper)
    val constructorApi = retrofit.create(ConstructorApi::class.java)
    return DataManager(constructorApi, moshi);
}
