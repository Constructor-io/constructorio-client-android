package io.constructor.injection.module

import android.content.Context
import dagger.Module
import dagger.Provides
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.injection.ApplicationContext
import javax.inject.Singleton

/**
 * @suppress
 */
@Module(includes = [(ApiModule::class)])
class AppModule(private val application: Context) {

    @Provides
    @ApplicationContext
    internal fun provideContext(): Context {
        return application
    }

    @Provides
    internal fun providePreferenceHelper(@ApplicationContext context: Context): PreferencesHelper {
        return PreferencesHelper(context)
    }

    @Provides
    @Singleton
    internal fun provideConfigMemoryHolder(): ConfigMemoryHolder {
        return ConfigMemoryHolder()
    }
}