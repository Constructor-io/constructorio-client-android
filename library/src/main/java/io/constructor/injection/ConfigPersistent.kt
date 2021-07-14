package io.constructor.injection


import io.constructor.injection.component.ConfigPersistentComponent
import javax.inject.Scope

/**
 * @suppress
 * A scoping annotation to permit dependencies confirm to the life of the
 * [ConfigPersistentComponent]
 */
@Scope @Retention annotation class ConfigPersistent