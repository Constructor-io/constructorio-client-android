package io.constructor.sample.feature.cart

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import io.constructor.data.model.search.SearchResponseInner
import io.constructor.sample.R
import io.constructor.sample.extensions.price
import kotlinx.android.synthetic.main.item_cart_content.view.*

class CartContentAdapter(var clickListener: (Pair<SearchResponseInner, Int>) -> Unit, val decrementAction: (SearchResponseInner) -> Unit, val incrementAction: (SearchResponseInner) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<Pair<SearchResponseInner, Int>>()

    fun setData(data: LinkedHashMap<String, Pair<SearchResponseInner, Int>>) {
        this.data.clear()
        this.data.addAll(data.map { it.value })
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        return CartContentViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.item_cart_content, p0, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
        val item = data[p1]
        with(p0 as CartContentViewHolder) {
            Glide.with(image).load(item.first.result.imageUrl).diskCacheStrategy(DiskCacheStrategy.ALL).into(image)
            title.text = item.first.value
            price.text = price.context.getString(R.string.price, (item.first.result.price() ?: 0.0) * item.second)
            increment.setOnClickListener {
                incrementAction.invoke(item.first)
            }
            decrement.setOnClickListener {
                decrementAction.invoke(item.first)
            }
            itemCount.text = item.second.toString()
            rootView.setOnClickListener {
                clickListener.invoke(item)
            }
        }
    }

    internal class CartContentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rootView = itemView.rootView
        val image = itemView.image
        val title = itemView.title
        val price = itemView.price
        val itemCount = itemView.itemCount
        val increment = itemView.increment
        val decrement = itemView.decrement
    }

}