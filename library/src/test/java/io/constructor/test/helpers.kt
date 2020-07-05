package io.constructor.test

import android.content.Context
import io.constructor.data.DataManager
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.remote.ConstructorApi
import io.constructor.injection.module.NetworkModule

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
    val okHttpClient = networkModule.provideOkHttpClient(loggingInterceptor, requestInterceptor)
    val retrofit = networkModule.provideRetrofit(okHttpClient, moshi)
    val constructorApi = retrofit.create(ConstructorApi::class.java)
    return DataManager(constructorApi, moshi);
}