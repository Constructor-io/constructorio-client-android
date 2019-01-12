package io.constructor.sample

import android.app.Application
import io.constructor.core.ConstructorIo
import io.constructor.core.ConstructorIoConfig

class SampleApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ConstructorIo.init(this, ConstructorIoConfig("key_OucJxxrfiTVUQx0C",
                testCells = listOf("ab" to "cd", "11" to "22")))
        ConstructorIo.userId = "uid"
    }
}