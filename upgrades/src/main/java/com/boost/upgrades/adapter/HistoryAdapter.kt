package com.boost.upgrades.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.boost.upgrades.R
import com.boost.upgrades.data.model.WidgetModel
import com.boost.upgrades.interfaces.HistoryFragmentListener


class HistoryAdapter(itemList: List<WidgetModel>?,val listener: HistoryFragmentListener) :
        RecyclerView.Adapter<HistoryAdapter.upgradeViewHolder>(), View.OnClickListener {

    private var list = ArrayList<WidgetModel>()
    private lateinit var context: Context

    init {
        this.list = itemList as ArrayList<WidgetModel>
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): upgradeViewHolder {
        val itemView = LayoutInflater.from(parent?.context).inflate(
                R.layout.history_item, parent, false
        )
        context = itemView.context


        itemView.setOnClickListener(this)
        return upgradeViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return 8 //list.size
    }

    override fun onBindViewHolder(holder: upgradeViewHolder, position: Int) {

        holder.viewButton.setOnClickListener {
                listener.viewHistoryItem(position)
        }
    }

    override fun onClick(v: View?) {

    }

    fun addupdates(upgradeModel: List<WidgetModel>) {
        val initPosition = list.size
        list.clear()
        list.addAll(upgradeModel)
        notifyItemRangeInserted(initPosition, list.size)
    }

    class upgradeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var viewButton = itemView.findViewById<TextView>(R.id.history_viewButton)!!
        var context: Context = itemView.context
//
//
//        fun upgradeListItem(updateModel: UpdatesModel) {
//
//        }
    }
}