package io.constructor.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import io.constructor.core.Constants
import io.constructor.core.ConstructorIo
import io.constructor.data.model.common.Result;

/**
 * @suppress
 * Service which triggers each time user select suggestion
 */
class OnSelectService : IntentService("OnSelectService") {

    companion object {
        fun startService(context: Context, query: String, suggestion: Result) {
            val intent = Intent(context, OnSelectService::class.java)
            intent.putExtra(Constants.EXTRA_QUERY, query)
            intent.putExtra(Constants.EXTRA_SUGGESTION, suggestion)
            context.startService(intent)
        }
    }


    override fun onHandleIntent(intent: Intent) {
        val query: String = intent.getStringExtra(Constants.EXTRA_QUERY)
        val suggestion: Result = intent.getSerializableExtra(Constants.EXTRA_SUGGESTION) as Result
        if (!suggestion.value.isBlank()) {
            ConstructorIo.trackAutocompleteSelect(suggestion.value, query, "Search Suggestions", suggestion.data.groups?.get(0))
        }
    }
}