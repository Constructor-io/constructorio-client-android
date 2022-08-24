package io.constructor.injection

import javax.inject.Qualifier

/**
 * Prevents unintentional leakage of Constructor.io instances into the user's dependency graph.
 */
@Qualifier @Retention annotation class ConstructorSdk
