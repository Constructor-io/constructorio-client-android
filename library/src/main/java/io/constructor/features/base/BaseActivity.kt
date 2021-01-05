package io.constructor.features.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.collection.LongSparseArray
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import io.constructor.core.ConstructorIo
import io.constructor.injection.component.ActivityComponent
import io.constructor.injection.component.ConfigPersistentComponent
import io.constructor.injection.component.DaggerConfigPersistentComponent
import io.constructor.injection.module.ActivityModule
import io.constructor.util.d
import java.util.concurrent.atomic.AtomicLong

abstract class BaseActivity : AppCompatActivity() {

    private var activityComponent: ActivityComponent? = null
    private var activityId = 0L

    companion object {
        private val KEY_ACTIVITY_ID = "KEY_ACTIVITY_ID"
        private val NEXT_ID = AtomicLong(0)
        private val componentsArray = LongSparseArray<ConfigPersistentComponent>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId())
        // Create the ActivityComponent and reuses cached ConfigPersistentComponent if this is
        // being called after a configuration change.
        activityId = savedInstanceState?.getLong(KEY_ACTIVITY_ID) ?: NEXT_ID.getAndIncrement()
        val configPersistentComponent: ConfigPersistentComponent
        if (componentsArray.get(activityId) == null) {
            d("Creating new ConfigPersistentComponent id=${activityId}")
            configPersistentComponent = DaggerConfigPersistentComponent.builder()
                    .appComponent(ConstructorIo.component)
                    .build()
            componentsArray.put(activityId, configPersistentComponent)
        } else {
            d("Reusing ConfigPersistentComponent id=${activityId}")
            configPersistentComponent = componentsArray.get(activityId)!!
        }
        activityComponent = configPersistentComponent.activityComponent(ActivityModule(this))
        activityComponent?.inject(this)
    }

    @LayoutRes
    abstract fun layoutId(): Int

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(KEY_ACTIVITY_ID, activityId)
    }

    override fun onDestroy() {
        if (!isChangingConfigurations) {
            d("Clearing ConfigPersistentComponent id=${activityId}")
            componentsArray.remove(activityId)
        }
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    fun activityComponent() = activityComponent as ActivityComponent
}
