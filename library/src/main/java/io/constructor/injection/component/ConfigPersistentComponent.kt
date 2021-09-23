package io.constructor.injection.component

import dagger.Component
import io.constructor.features.base.BaseActivity
import io.constructor.features.base.BaseFragment
import io.constructor.injection.ConfigPersistent
import io.constructor.injection.module.ActivityModule
import io.constructor.injection.module.FragmentModule

/**
 * @suppress
 * A dagger component that will live during the lifecycle of an Activity or Fragment but it won't
 * be destroy during configuration changes. Check [BaseActivity] and [BaseFragment] to
 * see how this components survives configuration changes.
 * Use the [ConfigPersistent] scope to annotate dependencies that need to survive
 * configuration changes (for example Presenters).
 */
@ConfigPersistent
@Component(dependencies = [AppComponent::class])
interface ConfigPersistentComponent {

    fun activityComponent(activityModule: ActivityModule): ActivityComponent

    fun fragmentComponent(fragmentModule: FragmentModule): FragmentComponent

}
