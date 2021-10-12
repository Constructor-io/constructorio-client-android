package io.constructor.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import io.constructor.core.Constants
import io.constructor.core.ConstructorIo
import io.constructor.data.model.common.Result;

/**
 * @suppress
 */
class OnSearchService : IntentService("OnSearchService") {

    companion object {
        fun startService(context: Context, query: String, suggestion: Result) {
            val intent = Intent(context, OnSearchService::class.java)
            intent.putExtra(Constants.EXTRA_QUERY, query)
            intent.putExtra(Constants.EXTRA_SUGGESTION, suggestion)
            context.startService(intent)
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        val suggestion: Result = intent?.getSerializableExtra(Constants.EXTRA_SUGGESTION) as Result
            if (!suggestion.value.isBlank()) {
                intent?.getStringExtra(Constants.EXTRA_QUERY)?.let { ConstructorIo.trackSearchSubmit(suggestion.value, it, suggestion.data.groups?.get(0)) }
            }
    }
}