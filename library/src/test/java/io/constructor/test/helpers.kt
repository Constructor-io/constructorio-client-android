package io.constructor.test

import android.content.Context
import io.constructor.data.DataManager
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.remote.ConstructorApi
import io.constructor.injection.module.NetworkModule
import java.util.concurrent.TimeUnit

/**
 * Creates a data manager that communicates with a Mock Web Server
 */
fun createTestDataManager(preferencesHelper: PreferencesHelper,
                          configMemoryHolder: ConfigMemoryHolder,
                          ctx: Context
): DataManager {
    val networkModule = NetworkModule(ctx);
    val loggingInterceptor = networkModule.provideHttpLoggingInterceptor()
    val requestInterceptor = networkModule.provideRequestInterceptor(preferencesHelper, configMemoryHolder)
    val moshi = networkModule.provideMoshi()
    val okHttpClient = networkModule.provideOkHttpClient(loggingInterceptor, requestInterceptor).newBuilder().addInterceptor { chain ->
        var request = chain.request()
        chain.proceed(request)
    }.readTimeout(1, TimeUnit.SECONDS).build()
    val retrofit = networkModule.provideRetrofit(okHttpClient, moshi, preferencesHelper)
    val constructorApi = retrofit.create(ConstructorApi::class.java)
    return DataManager(constructorApi, moshi);
}