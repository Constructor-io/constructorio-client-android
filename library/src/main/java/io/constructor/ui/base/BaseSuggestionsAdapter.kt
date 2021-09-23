package io.constructor.ui.base

import android.graphics.Color
import android.graphics.Typeface
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.constructor.util.ViewUtil
import io.constructor.data.model.common.Result

import java.util.*

/**
 * @suppress
 */
abstract class BaseSuggestionsAdapter : RecyclerView.Adapter<BaseSuggestionsAdapter.ViewHolder>() {

    private var suggestions: List<Result> = ArrayList()
    private var listener: BaseSuggestionsAdapter.ClickListener? = null

    @get:LayoutRes
    abstract val itemLayoutId: Int

    @get:IdRes
    abstract val suggestionNameId: Int

    @get:IdRes
    abstract val suggestionGroupNameId: Int

    abstract val styleHighlightedSpans: ((spannable: Spannable, spanStart: Int, spanEnd: Int) -> Unit)?

    interface ClickListener {
        fun onSuggestionClick(suggestion: Result)
    }

    fun setData(suggestions: List<Result>) {
        this.suggestions = suggestions
    }

    fun setListener(listener: ClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseSuggestionsAdapter.ViewHolder {
        val view = LayoutInflater
                .from(parent.context)
                .inflate(itemLayoutId, parent, false)
        return BaseSuggestionsAdapter.ViewHolder(view, suggestionNameId, suggestionGroupNameId)
    }

    abstract fun onViewTypeSuggestion(holder: ViewHolder, suggestion: String, highlightedSuggestion: Spannable, groupName: String?)

    override fun onBindViewHolder(holder: BaseSuggestionsAdapter.ViewHolder, position: Int) {
        val suggestionViewModel = suggestions[position]
        val text: String
        text = suggestionViewModel.value
        onViewTypeSuggestion(holder, text, ViewUtil.reverseHighlight(text, suggestionViewModel.matchedTerms) { spannable, start, end ->
            styleHighlightedSpans?.invoke(spannable, start, end) ?: styleSpans(spannable, start, end)

        }, suggestionViewModel.data.groups?.get(0)?.displayName)
        holder.itemView.setOnClickListener { listener!!.onSuggestionClick(suggestionViewModel) }
    }

    private fun styleSpans(spannable: Spannable, start: Int, end: Int) {
        spannable.setSpan(StyleSpan(Typeface.BOLD), start, end, 0)
        spannable.setSpan(ForegroundColorSpan(Color.BLACK), start, end, 0)
    }

    override fun getItemCount(): Int {
        return suggestions.size
    }

    class ViewHolder internal constructor(itemView: View, suggestionNameId: Int, suggestionGroupNameId: Int) : RecyclerView.ViewHolder(itemView) {
        var suggestionName: TextView = itemView.findViewById(suggestionNameId)
        var suggestionGroupName: TextView = itemView.findViewById(suggestionGroupNameId)
    }

}
