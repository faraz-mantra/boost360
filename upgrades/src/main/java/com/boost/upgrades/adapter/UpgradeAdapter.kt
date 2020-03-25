package com.boost.upgrades.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.boost.upgrades.R
import com.boost.upgrades.UpgradeActivity
import com.boost.upgrades.data.model.UpdatesModel
import com.boost.upgrades.ui.details.DetailsFragment
import com.boost.upgrades.utils.Constants
import com.bumptech.glide.Glide


class UpgradeAdapter(
    val activity: UpgradeActivity,
    cryptoCurrencies: List<UpdatesModel>?
) : RecyclerView.Adapter<UpgradeAdapter.upgradeViewHolder>() {

    private var upgradeList = ArrayList<UpdatesModel>()
    private lateinit var context: Context

    init {
        this.upgradeList = cryptoCurrencies as ArrayList<UpdatesModel>
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): upgradeViewHolder {
        val itemView = LayoutInflater.from(parent?.context).inflate(
            R.layout.upgrade_list_item, parent, false
        )
        context = itemView.context



        return upgradeViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return 4 //upgradeList.size
    }

    override fun onBindViewHolder(holder: upgradeViewHolder, position: Int) {
        val cryptocurrencyItem = upgradeList[position]
        holder.upgradeListItem(cryptocurrencyItem)

        holder.itemView.setOnClickListener {
            val details = DetailsFragment.newInstance()
            val args = Bundle()
            args.putInt("itemId", upgradeList.get(position).id.toInt())
            details.arguments = args
            activity.addFragment(details, Constants.DETAILS_FRAGMENT)
//            val intent = Intent(this.context, Details::class.java)
//            intent.putExtra("position",position)
//            startActivity(this.context, intent, null)
        }
    }

    fun addupdates(upgradeModel: List<UpdatesModel>) {
        val initPosition = upgradeList.size
        upgradeList.clear()
        upgradeList.addAll(upgradeModel)
        notifyItemRangeInserted(initPosition, upgradeList.size)
    }

    class upgradeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var upgradeTitle = itemView.findViewById<TextView>(R.id.title)!!
        private var upgradeDetails = itemView.findViewById<TextView>(R.id.details)!!
        private var upgradePrice = itemView.findViewById<TextView>(R.id.price)!!
        private var image = itemView.findViewById<ImageView>(R.id.imageView2)!!

        private var context: Context = itemView.context


        fun upgradeListItem(updateModel: UpdatesModel) {
            upgradeTitle.text = updateModel.title
            upgradeDetails.text = updateModel.name
            upgradePrice.text = "₹" + updateModel.price.toString() + "/month"
            Glide.with(context).load(updateModel.image).into(image)

        }
    }
}