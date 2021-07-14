package io.constructor.sample

import io.constructor.R
import io.constructor.ui.base.BaseSuggestionFragment
import io.constructor.ui.base.BaseSuggestionsAdapter
import io.constructor.ui.suggestion.SuggestionsAdapter

/**
 * @suppress
 */
class SuggestionsFragment : BaseSuggestionFragment() {

    override fun getProgressId(): Int {
        return 0
    }

    override fun layoutId(): Int {
        return R.layout.fragment_suggestions
    }

    override fun getSuggestionAdapter(): BaseSuggestionsAdapter {
        return SuggestionsAdapter()
    }

    override fun getSuggestionsInputId(): Int {
        return R.id.suggestions_box
    }

    override fun getSuggestionListId(): Int {
        return R.id.suggestionItems
    }

}
