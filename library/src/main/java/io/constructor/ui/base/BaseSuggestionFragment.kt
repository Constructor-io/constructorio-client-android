package io.constructor.ui.base

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import io.constructor.core.Constants
import io.constructor.core.ConstructorListener
import io.constructor.data.model.autocomplete.AutocompleteResponse
import io.constructor.data.model.common.Result
import io.constructor.features.base.BaseFragment
import io.constructor.mapper.Mapper
import io.constructor.service.OnSearchService
import io.constructor.service.OnSelectService
import io.constructor.ui.suggestion.SuggestionsPresenter
import io.constructor.ui.suggestion.SuggestionsView
import io.constructor.util.broadcastIntent
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * @suppress
 * abstract class you can extend to use your own custom ui
 */
abstract class BaseSuggestionFragment : BaseFragment(), SuggestionsView {

    private val searchSubject = PublishSubject.create<String>()
    private val focusChange = PublishSubject.create<Pair<String?, Boolean>>()
    private var listener: ConstructorListener? = null
    private var suggestionList: RecyclerView? = null
    private var suggestionBox: EditText? = null
    private var progressIndicator: ProgressBar? = null

    @Inject
    lateinit var presenter: SuggestionsPresenter

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            p1?.let {
                when (it.action) {
                    Constants.EVENT_QUERY_SENT -> it.getStringExtra(Constants.EXTRA_TERM)?.let { it1 -> listener?.onQuerySentToServer(it1) }
                    Constants.EVENT_SUGGESTIONS_RETRIEVED -> listener?.onSuggestionsRetrieved(it.getSerializableExtra(Constants.EXTRA_SUGGESTIONS) as List<Result>)
                    else -> {
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentComponent().inject(this)
        presenter.attachView(this)
        retainInstance = true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        suggestionList = view?.findViewById(getSuggestionListId())
        suggestionBox = view?.findViewById(getSuggestionsInputId())
        suggestionBox?.setOnFocusChangeListener { v, hasFocus ->
            val text = (v as EditText).text.toString()
            focusChange.onNext(Pair(if (text.isNullOrBlank()) null else text, hasFocus))
        }
        progressIndicator = view?.findViewById(getProgressId())
        suggestionList?.layoutManager = LinearLayoutManager(context)
        suggestionList?.adapter = getSuggestionAdapter()
        activity?.let {
            val filter = IntentFilter(Constants.EVENT_QUERY_SENT)
            filter.addAction(Constants.EVENT_SUGGESTIONS_RETRIEVED)
            LocalBroadcastManager.getInstance(it).registerReceiver(broadcastReceiver, filter)
        }
        suggestionBox?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!TextUtils.isEmpty(s)) {
                    searchSubject.onNext(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable) {
            }
        })

        suggestionBox?.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = suggestionBox?.text.toString()
                if (!TextUtils.isEmpty(query)) {
                    triggerSearch()
                }
                return@OnEditorActionListener true
            }

            return@OnEditorActionListener false
        })
//        backIcon?.setOnClickListener { activity?.finish() }
//        closeIcon?.setOnClickListener {
//            suggestionBox?.text?.clear()
//            clearSuggestions()
//        }
    }

    fun triggerSearch() {
        presenter.getSuggestions(suggestionBox?.text.toString())
    }

    fun clearSuggestions() {
        suggestionList?.layoutManager = LinearLayoutManager(context)
        suggestionList?.adapter = getSuggestionAdapter()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
        activity?.let {
            LocalBroadcastManager.getInstance(it).unregisterReceiver(broadcastReceiver)
        }
    }


    abstract fun getSuggestionAdapter(): BaseSuggestionsAdapter

    abstract fun getSuggestionsInputId(): Int

    abstract fun getSuggestionListId(): Int

    abstract fun getProgressId(): Int

    override fun showSuggestions(response: AutocompleteResponse, groupsShownForFirstTerm: Int) {
        progressIndicator?.visibility = View.GONE
        activity?.let {
            it.broadcastIntent(Constants.EVENT_SUGGESTIONS_RETRIEVED, Constants.EXTRA_SUGGESTIONS to response)
        }
        val suggestionViews = Mapper.toSuggestionsViewModel(response, groupsShownForFirstTerm)
        val adapter = getSuggestionAdapter()
        adapter.setData(suggestionViews)
        adapter.setListener(object : BaseSuggestionsAdapter.ClickListener {
            override fun onSuggestionClick(suggestion: Result) {
                listener?.onSuggestionSelected(suggestion.value, null, null)
                val query = suggestionBox?.text.toString()
                OnSelectService.startService(context!!, query, suggestion)
                OnSearchService.startService(context!!, query, suggestion)
            }
        })
        suggestionList?.adapter = adapter
    }

    override fun queryChanged(): Observable<String> {
        return searchSubject
    }

    override fun inputFocusChanged(): Observable<Pair<String?, Boolean>> {
        return focusChange
    }

    override fun onError(error: Throwable) {
        progressIndicator?.visibility = View.GONE
        error.printStackTrace()
        listener?.onErrorGettingSuggestions(error)
    }

    override fun loading() {
        progressIndicator?.visibility = View.VISIBLE
    }

    fun setConstructorListener(listener: ConstructorListener) {
        this.listener = listener
    }

}
