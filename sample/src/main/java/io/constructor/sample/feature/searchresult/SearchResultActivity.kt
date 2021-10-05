package io.constructor.sample.feature.searchresult

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.constructor.data.model.common.Result
import io.constructor.data.model.search.SearchResponseInner
import io.constructor.sample.R
import io.constructor.sample.common.BaseActivity
import io.constructor.sample.feature.productdetail.ProductDetailActivity
import io.constructor.sample.feature.searchresult.filterdialog.FilterDialog
import io.constructor.sample.feature.searchresult.sortdialog.SortDialog
//import kotlinx.android.synthetic.main.activity_home.*

class SearchResultActivity : BaseActivity<SearchResultPresenter>(), SearchView {

    private lateinit var adapter: SearchResultAdapter

    private val query by lazy {
        intent.getStringExtra(EXTRA_SEARCH_QUERY)
    }

    override fun renderData(it: SearchResponseInner, totalCount: Int) {
        adapter.setData(it)
//        toolbar.title = "$query, found: $totalCount"
    }

    override fun clearData() {
        adapter.clearData()
    }

    override fun initPresenter(): SearchResultPresenter {
        return SearchResultPresenter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_result)
        adapter = SearchResultAdapter {
//            presenter.handleClick(it, query)
        }
//        searchResult.adapter = adapter
//        searchResult.layoutManager = GridLayoutManager(this, 2)
        addIniniteScrollToRecycler()
        setupToolbar()
//        presenter.onCreate(query)
    }

    private fun setupToolbar() {
//        toolbar.inflateMenu(R.menu.search_result)
//        toolbar.title = query
//        toolbar.navigationIcon = null
//        toolbar.setOnMenuItemClickListener {
//            when (it.itemId) {
//                R.id.sort -> {
//                    presenter.availableFacets?.let {
//                        val dialog = FilterDialog.newInstance(ArrayList(it), presenter.selectedFacets)
//                        dialog.dismissListener = {
//                            presenter.facetsSelected(it)
//                        }
//                        dialog.show(supportFragmentManager, DIALOG_TAG)
//                    }
//                    true
//                }
//                R.id.filter -> {
//                    presenter.availableFilterSortOptions?.let {
//                        val dialog = SortDialog.newInstance(ArrayList(it), presenter.selectedFilterSortOption)
//                        dialog.dismissListener = {
//                            presenter.sortOptionSelected(it)
//                        }
//                        dialog.show(supportFragmentManager, DIALOG_TAG)
//                    }
//                    true
//                }
//                else -> {
//                    false
//                }
//            }
//        }
    }

    private fun addIniniteScrollToRecycler() {
//        searchResult.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                if (!recyclerView.canScrollVertically(1)) {
//                    presenter.loadNextDataBatch(false)
//                }
//            }
//
//        })
    }

    override fun navigateToDetails(it: Result) {
//        ProductDetailActivity.start(this, it)
    }

    companion object {

        const val DIALOG_TAG = "dialog"

        const val EXTRA_SEARCH_QUERY = "query"

        fun start(context: Context, query: String) {
            context.startActivity(Intent(context, SearchResultActivity::class.java).apply {
                putExtra(EXTRA_SEARCH_QUERY, query)
            })
        }
    }

}
