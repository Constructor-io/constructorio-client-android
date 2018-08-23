package io.constructor.sample

import android.app.Application
import io.constructor.core.ConstructorIo

class SampleApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ConstructorIo.init(this, "key_OucJxxrfiTVUQx0C")
        ConstructorIo.setTestCellValues("ab" to "cd", "11" to "22")
    }
}