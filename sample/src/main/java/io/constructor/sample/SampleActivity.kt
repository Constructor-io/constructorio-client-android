package io.constructor.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.constructor.core.ConstructorIo

import io.constructor.core.ConstructorListener
import io.constructor.data.model.Group
import io.constructor.data.model.Suggestion

class SampleActivity : AppCompatActivity() {

    private val TAG = this.javaClass.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_suggestions) as SuggestionsFragment
        fragment.setConstructorListener(object : ConstructorListener {
            override fun onErrorGettingSuggestions(error: Throwable) {
                Log.d(TAG, "handle network error getting suggestion")
            }

            override fun onSuggestionsRetrieved(suggestions: List<Suggestion>) {
                Log.d(TAG, "onSuggestionsRetrieved")
            }

            override fun onQuerySentToServer(query: String) {
                Log.d(TAG, "onQuerySentToServer")
            }

            override fun onSuggestionSelected(term: String, group: Group?, autocompleteSection: String?) {
                Log.d(TAG, "onSuggestionSelected")
            }
        })
    }
}
