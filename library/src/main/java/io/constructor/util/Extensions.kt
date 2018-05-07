package io.constructor.util

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
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
