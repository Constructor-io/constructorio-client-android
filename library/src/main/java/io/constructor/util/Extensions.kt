package io.constructor.util

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.util.Base64
import android.util.Log
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.Serializable
import java.net.URLEncoder

fun Context.broadcastIntent(action: String, vararg data: Pair<String, Any>) {
    Intent(action).apply {
        data.forEach { (key, value) -> setExtra(key, value) }
    }.let {
        LocalBroadcastManager.getInstance(this).sendBroadcast(it)
    }
}

fun Intent.setExtra(key: String, value: Any) {
    when (value) {
        is Int -> putExtra(key, value)
        is Long -> putExtra(key, value)
        is Float -> putExtra(key, value)
        is Double -> putExtra(key, value)
        is String -> putExtra(key, value)
        is Parcelable -> putExtra(key, value)
        is Serializable -> putExtra(key, value)
        else -> IllegalArgumentException("Cannot handle type ${value.javaClass.simpleName}")
    }
}

fun String.urlEncode() = URLEncoder.encode(this, "UTF-8").replace("+", "%20")

fun Any.d(msg: String) = Log.d(this::class.qualifiedName, msg)

fun Any.e(msg: String) = Log.e(this::class.qualifiedName, msg)

fun String.base64Encode(): String? {
    return String(Base64.encode(toByteArray(), Base64.NO_WRAP or Base64.NO_PADDING))
}

fun String.base64Decode(): String {
    return String(Base64.decode(this, Base64.NO_WRAP or Base64.NO_PADDING))
}

fun <T : Any> Observable<T>.io2ui(): Observable<T> {
    return compose {
        it.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }
}
