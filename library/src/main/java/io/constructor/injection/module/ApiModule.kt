package io.constructor.injection.module

import dagger.Module
import dagger.Provides
import io.constructor.data.remote.ConstructorApi
import retrofit2.Retrofit
import javax.inject.Singleton


@Module(includes = [(NetworkModule::class)])
class ApiModule {

    @Provides
    @Singleton
    internal fun provideConstructorApi(retrofit: Retrofit): ConstructorApi =
            retrofit.create(ConstructorApi::class.java)
}