package io.constructor.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import io.constructor.core.Constants
import io.constructor.core.ConstructorIo
import io.constructor.data.model.common.Result;

class OnSearchService : IntentService("OnSearchService") {

    companion object {
        fun startService(context: Context, query: String, suggestion: Result) {
            val intent = Intent(context, OnSearchService::class.java)
            intent.putExtra(Constants.EXTRA_QUERY, query)
            intent.putExtra(Constants.EXTRA_SUGGESTION, suggestion)
            context.startService(intent)
        }
    }

    override fun onHandleIntent(intent: Intent) {
        val query: String = intent.getStringExtra(Constants.EXTRA_QUERY)
        val suggestion: Result = intent.getSerializableExtra(Constants.EXTRA_SUGGESTION) as Result
            if (!suggestion.value.isBlank()) {
                ConstructorIo.trackSearchSubmit(suggestion.value, query, suggestion.data.groups?.get(0))
            }
    }
}