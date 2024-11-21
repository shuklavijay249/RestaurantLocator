import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.vijay.restaurant.R
import com.vijay.restaurant.data.Business

class RestaurantAdapter(
    private val loadMore: (() -> Unit)? = null
) : ListAdapter<Business, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    private var isLoading = false

    fun showLoading(show: Boolean) {
        isLoading = show
        notifyItemChanged(currentList.size)
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
        if (holder is RestaurantViewHolder && position < currentList.size) {
            holder.bind(getItem(position))
        }

        // Trigger load more when reaching the last item
        if (position == itemCount - 1 && loadMore != null && !isLoading) {
            loadMore.invoke()
        }
    }

    override fun getItemCount(): Int = currentList.size + if (isLoading) 1 else 0

    override fun getItemViewType(position: Int): Int {
        return if (position < currentList.size) ITEM_TYPE else LOADING_TYPE
    }

    companion object {
        const val ITEM_TYPE = 1
        const val LOADING_TYPE = 2

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Business>() {
            override fun areItemsTheSame(oldItem: Business, newItem: Business): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Business, newItem: Business): Boolean {
                return oldItem == newItem
            }
        }
    }

    class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.text_name)
        private val addressText: TextView = itemView.findViewById(R.id.text_address)
        private val ratingText: TextView = itemView.findViewById(R.id.text_rating)
        private val imageView: ImageView = itemView.findViewById(R.id.image_restaurant)

        fun bind(business: Business) {
            nameText.text = business.name
            addressText.text = business.location?.fullAddress ?: "Unknown"
            ratingText.text = business.rating.toString()

            Glide.with(itemView.context)
                .load(business.image_url)
                .placeholder(R.drawable.placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
        }
    }

    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
