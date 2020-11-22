package io.constructor.sample.feature.searchresult.filterdialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import io.constructor.data.model.common.FilterFacet
import io.constructor.sample.R
import kotlinx.android.synthetic.main.dialog_filter.view.*

class FilterDialog : DialogFragment() {

    private lateinit var adapter: FilterListAdapter

    var dismissListener: ((HashMap<String, MutableList<String>>) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_filter, null)
        val dialog = AlertDialog.Builder(context!!)
                .setView(view)
                .create()
        setDialogInteractions(view)
        return dialog
    }


    private fun setDialogInteractions(view: View) {
        val list = arguments?.getSerializable(EXTRA_FACETS) as ArrayList<FilterFacet>
        val selectedFacets = arguments?.getSerializable(EXTRA_SELECTED_FACETS) as? HashMap<String, MutableList<String>>
        view.filterList.layoutManager = LinearLayoutManager(view.context)
        adapter = FilterListAdapter().apply {
            setData(list, selectedFacets)
        }
        view.title.text = "Filters"
        view.cancel.setOnClickListener {
            dismiss()
        }
        view.apply.setOnClickListener {
            dismissListener?.invoke(adapter.getSelected())
            dismiss()
        }
        view.filterList.adapter = adapter
    }

    companion object {

        const val EXTRA_FACETS = "facets"
        const val EXTRA_SELECTED_FACETS = "selected-facets"

        fun newInstance(facets: ArrayList<FilterFacet>, selectedFacets: HashMap<String, MutableList<String>>? = null): FilterDialog {
            return FilterDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(EXTRA_FACETS, facets)
                    selectedFacets?.let {
                        putSerializable(EXTRA_SELECTED_FACETS, selectedFacets)
                    }
                }
            }
        }
    }

}
