package io.constructor.injection.component

import io.constructor.features.base.BaseActivity
import io.constructor.injection.PerActivity
import io.constructor.injection.module.ActivityModule
import io.constructor.ui.base.BaseSuggestionFragment
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [ActivityModule::class])
interface ActivityComponent {
    fun inject(baseActivity: BaseActivity)
}
