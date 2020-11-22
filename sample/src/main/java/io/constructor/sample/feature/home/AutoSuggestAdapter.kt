package io.constructor.sample.feature.home

import android.content.Context
import android.widget.ArrayAdapter
import io.constructor.data.model.autocomplete.AutocompleteResponse
import io.constructor.data.model.common.Result

class AutoSuggestAdapter(context: Context, resource: Int) : ArrayAdapter<String>(context, resource) {

    private val data = mutableListOf<Result>()

    fun setData(suggestions: AutocompleteResponse) {
        data.clear()
        suggestions.sections?.get("Search Suggestions")?.let { data.addAll(it) }
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): String {
        return data[position].value
    }

    fun getSuggestion(position: Int): Result {
        return data[position]
    }

    override fun getCount(): Int {
        return data.size
    }
}