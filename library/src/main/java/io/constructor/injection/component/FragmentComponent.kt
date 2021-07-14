package io.constructor.injection.component

import io.constructor.injection.PerFragment
import io.constructor.injection.module.FragmentModule
import io.constructor.ui.base.BaseSuggestionFragment

import dagger.Subcomponent

/**
 * @suppress
 * This component inject dependencies to all Fragments across the application
 */
@PerFragment
@Subcomponent(modules = [FragmentModule::class])
interface FragmentComponent {
    fun inject(baseSuggestionFragment: BaseSuggestionFragment)
}