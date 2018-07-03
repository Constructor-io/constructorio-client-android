package io.constructor.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import io.constructor.core.Constants
import io.constructor.core.ConstructorIo
import io.constructor.data.model.SuggestionViewModel

class OnSearchService : IntentService("OnSearchService") {

    companion object {
        fun startService(context: Context, query: String, suggestion: SuggestionViewModel) {
            val intent = Intent(context, OnSearchService::class.java)
            intent.putExtra(Constants.EXTRA_QUERY, query)
            intent.putExtra(Constants.EXTRA_SUGGESTION, suggestion)
            context.startService(intent)
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        val query: String? = intent?.getStringExtra(Constants.EXTRA_QUERY)
        val suggestion: SuggestionViewModel = intent?.getSerializableExtra(Constants.EXTRA_SUGGESTION) as SuggestionViewModel
            if (!suggestion.term.isBlank()) {
                ConstructorIo.trackSearch(query!!, suggestion)
            }
    }
}