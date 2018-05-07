package io.constructor.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.constructor.core.ConstructorListener
import io.constructor.data.model.Group
import io.constructor.data.model.Suggestion
import io.constructor.ui.base.BaseSuggestionFragment
import io.constructor.ui.base.BaseSuggestionsAdapter
import io.constructor.ui.suggestion.SuggestionsAdapter
import kotlinx.android.synthetic.main.fragment_custom_suggestions.view.*

class SampleActivityCustom : AppCompatActivity() {

    private val TAG = this.javaClass.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample_custom)
        if (savedInstanceState == null) {
            val fragment = CustomSearchFragment()
            fragment.setConstructorListener(object : ConstructorListener {
                override fun onErrorGettingSuggestions(error: Throwable) {
                    Log.d(TAG, "onError")
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
            fragment.retainInstance = true
            supportFragmentManager.beginTransaction().add(R.id.content, fragment, "").commit()
        }
    }

    class CustomSearchFragment : BaseSuggestionFragment() {

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            view?.backButton?.setOnClickListener {
                view?.input?.text?.clear()
                clearSuggestions()
            }
            view?.searchButton?.setOnClickListener { triggerSearch() }
        }

        override fun getSuggestionAdapter(): BaseSuggestionsAdapter {
            return SuggestionsAdapter()
        }

        override fun getSuggestionsInputId(): Int {
            return R.id.input
        }

        override fun getSuggestionListId(): Int {
            return R.id.suggestions
        }

        override fun getProgressId(): Int {
            return 0
        }

        override fun layoutId(): Int {
            return R.layout.fragment_custom_suggestions
        }

    }
}
