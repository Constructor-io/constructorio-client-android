package io.constructor.sample.feature.home

import android.content.Context
import android.widget.ArrayAdapter
import io.constructor.data.model.Suggestion

class AutoSuggestAdapter(context: Context, resource: Int) : ArrayAdapter<String>(context, resource) {

    private val data = mutableListOf<Suggestion>()

    fun setData(suggestions: List<Suggestion>) {
        data.clear()
        data.addAll(suggestions)
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): String {
        return data[position].value
    }

    fun getSuggestion(position: Int): Suggestion {
        return data[position]
    }

    override fun getCount(): Int {
        return data.size
    }
}