package io.constructor.sample.feature.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import io.constructor.sample.R
import io.constructor.sample.common.BaseActivity
import io.constructor.sample.feature.cart.CartActivity
import io.constructor.sample.feature.searchresult.SearchResultActivity
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : BaseActivity<HomePresenter>(), HomeView {

    override fun renderAutcompleteData(it: List<Suggestion>) {
        autocompleteAdapter.setData(it)
        autocompleteAdapter.notifyDataSetChanged()
    }

    override fun initPresenter(): HomePresenter {
        return HomePresenter(this)
    }

    private lateinit var autocompleteAdapter: AutoSuggestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setupToolbar()
        setupSearch()
        presenter.onCreate()
    }

    private fun setupToolbar() {
        toolbar.inflateMenu(R.menu.home)
        toolbar.setTitle(R.string.app_name)
        toolbar.navigationIcon = null
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.search -> {
                    if (searchInput.visibility == View.GONE) {
                        searchInput.visibility = View.VISIBLE
                        searchInput.requestFocus()
                    } else {
                        if (searchInput.text.toString().isNotEmpty()) {
                            SearchResultActivity.start(this, searchInput.text.toString())
                        }
                    }

                    true
                }
                R.id.cart -> {
                    startActivity(Intent(this, CartActivity::class.java))
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    private fun setupSearch() {
        autocompleteAdapter = AutoSuggestAdapter(this, android.R.layout.simple_dropdown_item_1line)
        searchInput.setAdapter(autocompleteAdapter)
        searchInput.setOnItemClickListener { parent, view, position, id ->
            val item = autocompleteAdapter.getSuggestion(position)
            SearchResultActivity.start(this, item.value)
        }
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                presenter.onQuery(s.toString())
            }

        })
    }

}
