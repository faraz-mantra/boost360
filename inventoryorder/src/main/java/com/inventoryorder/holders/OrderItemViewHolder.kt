package com.inventoryorder.holders

import android.view.View
import androidx.core.content.ContextCompat
import com.framework.utils.DateUtils.FORMAT_SERVER_DATE
import com.framework.utils.DateUtils.FORMAT_SERVER_TO_LOCAL
import com.framework.utils.DateUtils.parseDate
import com.inventoryorder.R
import com.inventoryorder.constant.RecyclerViewActionType
import com.inventoryorder.databinding.ItemOrderBinding
import com.inventoryorder.model.ordersdetails.OrderItem
import com.inventoryorder.model.ordersummary.OrderSummaryModel
import com.inventoryorder.recyclerView.AppBaseRecyclerViewHolder
import com.inventoryorder.recyclerView.BaseRecyclerViewItem

class OrderItemViewHolder(binding: ItemOrderBinding) : AppBaseRecyclerViewHolder<ItemOrderBinding>(binding) {

  override fun bind(position: Int, item: BaseRecyclerViewItem) {
    super.bind(position, item)
    val data = item as? OrderItem
    binding.view.visibility = if (adapterPosition == 0) View.VISIBLE else View.GONE
    data?.let {
      binding.orderDate.title.text = activity?.resources?.getString(R.string.date_order)
      binding.payment.title.text = activity?.resources?.getString(R.string.payment)
      binding.delivery.title.text = activity?.resources?.getString(R.string.delivery)
      setDataResponse(it)
    }
    binding.mainView.setOnClickListener {
      listener?.onItemClick(adapterPosition, data, RecyclerViewActionType.ORDER_ITEM_CLICKED.ordinal)
    }
  }

  private fun setDataResponse(order: OrderItem) {
//    binding.orderType.text = OrderSummaryModel.OrderType.fromValue(order.status()).type
    binding.orderType.text = OrderSummaryModel.OrderType.fromType(order.status()).type
    binding.orderId.text = "# ${order.ReferenceNumber}"
    order.BillingDetails?.let { bill ->
      val currency = takeIf { bill.CurrencyCode.isNullOrEmpty().not() }?.let { bill.CurrencyCode?.trim() } ?: "INR"
      binding.txtRupees.text = "$currency ${bill.AmountPayableByBuyer}"
    }
    binding.orderDate.value.text = parseDate(order.CreatedOn, FORMAT_SERVER_DATE, FORMAT_SERVER_TO_LOCAL)
    binding.payment.value.text = order.PaymentDetails?.Method?.trim()
    binding.delivery.value.text = order.LogisticsDetails?.DeliveryMode?.trim()
    val sizeItem = order.Items?.size ?: 0
    binding.itemCount.text = "$sizeItem ${takeIf { sizeItem > 1 }?.let { "Items" } ?: "Item"}"
    binding.itemDesc.text = order.getTitles()

    binding.itemMore.visibility = takeIf { (sizeItem > 3) }?.let {
      binding.itemMore.text = "${sizeItem - 3} more"
      View.VISIBLE
    } ?: View.GONE

    when (OrderSummaryModel.OrderType.fromType(order.status())) {
      OrderSummaryModel.OrderType.RECEIVED,
      OrderSummaryModel.OrderType.SUCCESSFUL,
      OrderSummaryModel.OrderType.RETURNED,
      OrderSummaryModel.OrderType.ABANDONED,
      OrderSummaryModel.OrderType.ESCALATED -> {
        changeBackground(View.VISIBLE, View.VISIBLE, View.GONE, R.drawable.new_order_bg, R.color.watermelon_light)
      }
      OrderSummaryModel.OrderType.CANCELLED -> {
        changeBackground(View.GONE, View.GONE, View.VISIBLE, R.drawable.cancel_order_bg, R.color.primary_grey)
      }
    }
  }

  private fun changeBackground(confirm: Int, detaile: Int, btn: Int, orderBg: Int, rupeesColor: Int) {
    binding.btnConfirm.visibility = confirm
    binding.detailsOrder.visibility = detaile
    binding.next2.visibility = btn
    activity?.let {
      binding.orderType.background = ContextCompat.getDrawable(it, orderBg)
      binding.txtRupees.setTextColor(ContextCompat.getColor(it, rupeesColor))
    }
  }
}

//          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//           binding.timeElapsed.compoundDrawableTintList = ContextCompat.getColorStateList(it, R.color.warm_grey_10)
//          }