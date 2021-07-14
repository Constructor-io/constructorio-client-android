package io.constructor.injection.component

import android.content.Context
import dagger.Component
import io.constructor.data.DataManager
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.remote.ConstructorApi
import io.constructor.injection.ApplicationContext
import io.constructor.injection.module.AppModule
import javax.inject.Singleton

/**
 * @suppress
 */
@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    @ApplicationContext
    fun context(): Context

    fun dataManager(): DataManager

    fun preferenceHelper(): PreferencesHelper

    fun configMemoryHolder(): ConfigMemoryHolder

    fun constructorApi(): ConstructorApi
}
