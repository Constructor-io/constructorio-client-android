package io.constructor.sample.feature.searchresult.filterdialog

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.constructor.data.model.common.FilterFacetOption
import io.constructor.data.model.common.FilterFacet
import io.constructor.sample.R
import kotlinx.android.synthetic.main.item_facet.view.*
import kotlinx.android.synthetic.main.item_header.view.*

class FilterListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1

    private val data = mutableListOf<DataWrapper<*>>()

    fun setData(facets: ArrayList<FilterFacet>, selected: HashMap<String, MutableList<String>>? = null) {
        facets.forEach { facet ->
            val selectedFacets = selected?.get(facet.displayName!!)
            data.add(HeaderWrapper(facet))
            facet.options?.forEach {
                data.add(ItemWrapper(facet.displayName!! to it).apply {
                    this.checked = selectedFacets?.contains(it.value) == true
                })
            }
        }
        notifyDataSetChanged()
    }

    fun getSelected(): HashMap<String, MutableList<String>> {
        val result = hashMapOf<String, MutableList<String>>()
        data.forEach {
            if (it is ItemWrapper && it.checked) {
                if (result.containsKey(it.data.first)) {
                    result[it.data.first]?.add(it.data.second.value!!)
                } else {
                    result[it.data.first] = mutableListOf()
                    result[it.data.first]?.add(it.data.second.value!!)
                }
            }
        }
        return result
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        when (p1) {
            TYPE_HEADER -> {
                return HeaderViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.item_header, p0, false))
            }
            else -> {
                return ItemViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.item_facet, p0, false))
            }
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        val item = data[position]
        return when (item) {
            is HeaderWrapper -> TYPE_HEADER
            else -> TYPE_ITEM
        }
    }

    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
        val item = data[p1]
        when (p0) {
            is HeaderViewHolder -> {
                p0.title.text = (item as HeaderWrapper).data.displayName
            }
            is ItemViewHolder -> {
                item as ItemWrapper
                p0.checkbox.text = item.data.second.value
                p0.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                    item.checked = isChecked
                }
                p0.checkbox.isChecked = item.checked
            }
        }
    }

    internal class HeaderWrapper(data: FilterFacet) : DataWrapper<FilterFacet>(data)

    internal class ItemWrapper(data: Pair<String, FilterFacetOption>) : DataWrapper<Pair<String, FilterFacetOption>>(data)

    internal abstract class DataWrapper<T>(var data: T) {
        var checked = false
    }

    internal class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = itemView.title
    }

    internal class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkbox = itemView.checkBox
    }

}