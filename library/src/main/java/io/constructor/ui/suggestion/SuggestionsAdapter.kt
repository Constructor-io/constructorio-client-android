package io.constructor.ui.suggestion

import android.text.Spannable
import android.view.View

import io.constructor.R
import io.constructor.ui.base.BaseSuggestionsAdapter

/**
 * @suppress
 */
class SuggestionsAdapter : BaseSuggestionsAdapter() {

    override val styleHighlightedSpans = null

    override val itemLayoutId: Int
        get() = R.layout.list_item_suggestion

    override val suggestionNameId: Int
        get() = R.id.suggestionName

    override val suggestionGroupNameId: Int
        get() = R.id.suggestionGroupName

    override fun onViewTypeSuggestion(holder: BaseSuggestionsAdapter.ViewHolder, suggestion: String, highlightedSuggestion: Spannable, groupName: String?) {
        holder.suggestionName.text = highlightedSuggestion
        holder.suggestionGroupName.visibility = if (groupName != null) View.VISIBLE else View.GONE
        groupName?.let { holder.suggestionGroupName.text = holder.suggestionGroupName.context.getString(R.string.group_text, it) }
    }
}
