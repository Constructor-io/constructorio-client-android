package io.constructor.injection.module

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.injection.ApplicationContext
import io.constructor.injection.ConstructorSdk
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
    internal fun providePreferenceHelper(@ConstructorSdk preferences: SharedPreferences): PreferencesHelper {
        return PreferencesHelper(preferences)
    }

    @Provides
    @ConstructorSdk
    internal fun provideConfigPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("constructor_pref_file", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    internal fun provideConfigMemoryHolder(): ConfigMemoryHolder {
        return ConfigMemoryHolder()
    }
}
