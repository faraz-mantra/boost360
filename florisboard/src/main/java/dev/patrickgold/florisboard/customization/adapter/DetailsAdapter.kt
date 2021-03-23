package dev.patrickgold.florisboard.customization.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.customization.model.response.CustomerDetails
import dev.patrickgold.florisboard.customization.viewholder.DetailsViewHolder


class DetailsAdapter : ListAdapter<CustomerDetails, DetailsViewHolder>(DetailsItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): DetailsViewHolder {
        return DetailsViewHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.adapter_item_details, parent, false)
        )
    }

    override fun onBindViewHolder(holder: DetailsViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }
}

class DetailsItemDiffCallback : DiffUtil.ItemCallback<CustomerDetails>() {
    override fun areItemsTheSame(oldItem: CustomerDetails, newItem: CustomerDetails): Boolean = oldItem == newItem

    override fun areContentsTheSame(oldItem: CustomerDetails, newItem: CustomerDetails): Boolean = oldItem == newItem

}