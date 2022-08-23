package io.constructor.injection.module

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import io.constructor.BuildConfig
import io.constructor.data.interceptor.RequestInterceptor
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.model.dataadapter.ResultDataAdapter
import io.constructor.injection.ConstructorSdk
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

/**
 * @suppress
 */
@Module
object NetworkModule {

    @Provides
    @Singleton
    internal fun provideRetrofit(
        @ConstructorSdk okHttpClient: OkHttpClient,
        @ConstructorSdk moshi: Moshi,
        preferencesHelper: PreferencesHelper
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(preferencesHelper.scheme + "://" + preferencesHelper.serviceUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

    @Provides
    @Singleton
    @ConstructorSdk
    internal fun provideOkHttpClient(
        @ConstructorSdk httpLoggingInterceptor: HttpLoggingInterceptor,
        requestInterceptor: RequestInterceptor
    ): OkHttpClient {
        val httpClientBuilder = OkHttpClient.Builder()
        httpClientBuilder.addInterceptor(requestInterceptor)
        if (BuildConfig.DEBUG) {
            httpClientBuilder.addInterceptor(httpLoggingInterceptor)
        }
        return httpClientBuilder.build()

    }

    @Provides
    @Singleton
    @ConstructorSdk
    internal fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)

    @Provides
    @Singleton
    internal fun provideRequestInterceptor(
        prefHelper: PreferencesHelper,
        configMemoryHolder: ConfigMemoryHolder
    ): RequestInterceptor = RequestInterceptor(prefHelper, configMemoryHolder)

    @Provides
    @Singleton
    @ConstructorSdk
    internal fun provideMoshi(): Moshi = Moshi
        .Builder()
        .add(ResultDataAdapter())
        .build()
}
