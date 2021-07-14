package io.constructor.injection.module

import android.app.Activity
import android.content.Context

import dagger.Module
import dagger.Provides
import io.constructor.injection.ActivityContext

/**
 * @suppress
 */
@Module
class ActivityModule(private val activity: Activity) {

    @Provides
    internal fun provideActivity(): Activity {
        return activity
    }

    @Provides
    @ActivityContext
    internal fun providesContext(): Context {
        return activity
    }
}
