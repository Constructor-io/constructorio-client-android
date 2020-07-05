package io.constructor.sample

import android.app.Application
import io.constructor.core.ConstructorIo
import io.constructor.core.ConstructorIoConfig

class BodegaApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ConstructorIo.init(this, ConstructorIoConfig("key_K2hlXt5aVSwoI1Uw"))
        ConstructorIo.userId = "uid"
    }
}