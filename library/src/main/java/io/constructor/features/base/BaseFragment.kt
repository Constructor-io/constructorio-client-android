package io.constructor.features.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import android.util.LongSparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.constructor.core.ConstructorIo
import io.constructor.injection.component.ConfigPersistentComponent
import io.constructor.injection.component.DaggerConfigPersistentComponent
import io.constructor.injection.component.FragmentComponent
import io.constructor.injection.module.FragmentModule
import io.constructor.util.d
import java.util.concurrent.atomic.AtomicLong

/**
 * Abstract Fragment that every other Fragment in this application must implement. It handles
 * creation of Dagger components and makes sure that instances of ConfigPersistentComponent are kept
 * across configuration changes.
 */
abstract class BaseFragment : Fragment() {

    private var fragmentComponent: FragmentComponent? = null
    private var fragmentId = 0L

    companion object {
        private val KEY_FRAGMENT_ID = "KEY_FRAGMENT_ID"
        private val componentsArray = LongSparseArray<ConfigPersistentComponent>()
        private val NEXT_ID = AtomicLong(0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create the FragmentComponent and reuses cached ConfigPersistentComponent if this is
        // being called after a configuration change.
        fragmentId = savedInstanceState?.getLong(KEY_FRAGMENT_ID) ?: NEXT_ID.getAndIncrement()
        val configPersistentComponent: ConfigPersistentComponent
        if (componentsArray.get(fragmentId) == null) {
            d("Creating new ConfigPersistentComponent id=${fragmentId}")
            configPersistentComponent = DaggerConfigPersistentComponent.builder()
                    .appComponent(ConstructorIo.component)
                    .build()
            componentsArray.put(fragmentId, configPersistentComponent)
        } else {
            d("Reusing ConfigPersistentComponent id=$fragmentId")
            configPersistentComponent = componentsArray.get(fragmentId)
        }
        fragmentComponent = configPersistentComponent.fragmentComponent(FragmentModule(this))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(layoutId(), container, false)

    @LayoutRes
    abstract fun layoutId(): Int

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(KEY_FRAGMENT_ID, fragmentId)
    }

    override fun onDestroy() {
        if (!activity!!.isChangingConfigurations) {
            d("Clearing ConfigPersistentComponent id=$fragmentId")
            componentsArray.remove(fragmentId)
        }
        super.onDestroy()
    }

    fun fragmentComponent() = fragmentComponent as FragmentComponent
}
