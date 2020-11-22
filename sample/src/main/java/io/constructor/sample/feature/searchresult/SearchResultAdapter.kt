package io.constructor.sample.feature.searchresult

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import io.constructor.data.model.search.SearchResponseInner
import io.constructor.sample.R
import io.constructor.sample.extensions.priceFormatted
import kotlinx.android.synthetic.main.item_search_result.view.*

class SearchResultAdapter(var clickListener: (SearchResponseInner) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<SearchResponseInner>()

    fun setData(it: SearchData) {
        it.searchResults?.let {
            data.addAll(it)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        return SearchItemViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.item_search_result, p0, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
        val item = data[p1]
        with(p0 as SearchItemViewHolder) {
            Glide.with(image).load(item.result.imageUrl).into(image)
            title.text = item.value
            description.text = item.result.description
            val priceFormatted = item.result.priceFormatted()
            price.text = priceFormatted
            rootView.setOnClickListener {
                clickListener.invoke(item)
            }
        }
    }

    fun clearData() {
        data.clear()
        notifyDataSetChanged()
    }


    internal class SearchItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rootView = itemView.rootView
        val image = itemView.image
        val price = itemView.price
        val title = itemView.title
        val description = itemView.description
    }

}