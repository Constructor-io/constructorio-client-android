package io.constructor.runner

import io.constructor.util.RxIdlingExecutionHook
import io.constructor.util.RxIdlingResource
import android.os.Bundle
import android.support.test.espresso.IdlingRegistry
import rx.plugins.RxJavaPlugins

/**
 * Runner that registers a Espresso Indling resource that handles waiting for
 * RxJava Observables to finish.
 * WARNING - Using this runner will block the tests if the application uses long-lived hot
 * Observables such us event buses, etc.
 */
class RxAndroidJUnitRunner : UnlockDeviceAndroidJUnitRunner() {

    override fun onCreate(arguments: Bundle) {
        super.onCreate(arguments)
        val rxIdlingResource = RxIdlingResource()

//        RxJavaHooks.setOnObservableStart { t1, t2 ->
//            rxIdlingResource.incrementActiveSubscriptionsCount()
//
//
//        }


        RxJavaPlugins.getInstance()
                .registerObservableExecutionHook(RxIdlingExecutionHook(rxIdlingResource))
//        Espresso.registerIdlingResources(rxIdlingResource)
        IdlingRegistry.getInstance().register(rxIdlingResource)

    }
}