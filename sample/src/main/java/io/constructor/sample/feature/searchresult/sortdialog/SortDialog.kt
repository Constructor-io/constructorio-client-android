package io.constructor.sample.feature.searchresult.sortdialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import io.constructor.data.model.search.SortOption
import io.constructor.sample.R
import kotlinx.android.synthetic.main.dialog_sort.view.*

class SortDialog : DialogFragment() {

    var dismissListener: ((SortOption?) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_sort, null)
        val dialog = AlertDialog.Builder(context!!)
                .setView(view)
                .create()
        setDialogInteractions(view)
        return dialog
    }


    private fun setDialogInteractions(view: View) {
        val list = arguments?.getSerializable(EXTRA_OPTIONS) as ArrayList<SortOption>
        val selectedSortOption = arguments?.getSerializable(EXTRA_SELECTED_OPTION) as? SortOption
        list.forEachIndexed { index, sortOption ->
            val radioButton = LayoutInflater.from(view.context).inflate(R.layout.item_sort_option, view.filterRadioGroup, false) as RadioButton
            radioButton.id = index + 1
            radioButton.text = sortOption.displayName
            if (sortOption.displayName == selectedSortOption?.displayName) {
                radioButton.isChecked = true
            }
            view.filterRadioGroup.addView(radioButton)
        }
        view.title.text = "Sort options"
        view.clear.setOnClickListener {
            dismissListener?.invoke(null)
            dismiss()
        }
        view.apply.setOnClickListener {
            for (i in 0 until view.filterRadioGroup.childCount) {
                val child = view.filterRadioGroup.getChildAt(i) as RadioButton
                if (child.isChecked) {
                    dismissListener?.invoke(list[i])
                    break
                }
            }
            dismiss()
        }
    }

    companion object {

        const val EXTRA_OPTIONS = "options"
        const val EXTRA_SELECTED_OPTION = "selected-option"

        fun newInstance(sortOptions: ArrayList<SortOption>, selectedSortOption: SortOption? = null): SortDialog {
            return SortDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(EXTRA_OPTIONS, sortOptions)
                    selectedSortOption?.let {
                        putSerializable(EXTRA_SELECTED_OPTION, selectedSortOption)
                    }
                }
            }
        }
    }

}
