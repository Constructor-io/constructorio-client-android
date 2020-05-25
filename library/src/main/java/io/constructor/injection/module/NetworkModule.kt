package io.constructor.injection.module

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import io.constructor.BuildConfig
import io.constructor.data.interceptor.TokenInterceptor
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.model.dataadapter.ResultDataAdapter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton


@Module
class NetworkModule(private val context: Context) {

    @Provides
    @Singleton
    internal fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
            Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_API_URL)
                    .client(okHttpClient)
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()

    @Provides
    @Singleton
    internal fun provideOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor,
                                     tokenInterceptor: TokenInterceptor): OkHttpClient {
        val httpClientBuilder = OkHttpClient.Builder()
        httpClientBuilder.addInterceptor(tokenInterceptor)
        if (BuildConfig.DEBUG) {
            httpClientBuilder.addInterceptor(httpLoggingInterceptor)
        }
        return httpClientBuilder.build()

    }

    @Provides
    @Singleton
    internal fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    @Provides
    @Singleton
    internal fun provideTokenInterceptor(prefHelper: PreferencesHelper, configMemoryHolder: ConfigMemoryHolder): TokenInterceptor = TokenInterceptor(context, prefHelper, configMemoryHolder)

    @Provides
    @Singleton
    internal fun provideMoshi(): Moshi = Moshi
            .Builder()
            .add(ResultDataAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()
}