package com.vijay.restaurant.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.vijay.restaurant.data.Business
import com.vijay.restaurant.R

class RestaurantAdapter(private val loadMore: () -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val restaurants = mutableListOf<Business>()
    private var isLoading = false

    fun submitList(newRestaurants: List<Business>) {
        restaurants.clear()
        restaurants.addAll(newRestaurants)
        notifyDataSetChanged()
    }

    fun showLoading(show: Boolean) {
        isLoading = show
        notifyItemChanged(restaurants.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_TYPE) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant, parent, false)
            RestaurantViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false)
            LoadingViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RestaurantViewHolder) {
            holder.bind(restaurants[position])
        }

        if (position == restaurants.size - 1 && !isLoading) {
            loadMore()
        }
    }

    override fun getItemCount(): Int = restaurants.size + if (isLoading) 1 else 0

    override fun getItemViewType(position: Int): Int {
        return if (position < restaurants.size) ITEM_TYPE else LOADING_TYPE
    }

    companion object {
        const val ITEM_TYPE = 1
        const val LOADING_TYPE = 2
    }

    class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(business: Business) {
            itemView.findViewById<TextView>(R.id.text_name).text = business.name

            val addressText = buildString {
                business.location.fullAddress?.let {
                    append(", ${it.toString()}")
                }
            }
            itemView.findViewById<TextView>(R.id.text_address).text = addressText

            itemView.findViewById<TextView>(R.id.text_rating).text = business.rating.toString()

            business.image_url?.let {
                Glide.with(itemView.context).load(it).diskCacheStrategy(DiskCacheStrategy.ALL).into(itemView.findViewById(R.id.image_restaurant))
            } ?: run {
                Glide.with(itemView.context).load(R.drawable.placeholder).into(itemView.findViewById(
                    R.id.image_restaurant
                ))
            }
        }
    }

    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
