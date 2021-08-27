package com.appservice.model.catalog

import com.appservice.R
import com.appservice.constant.RecyclerViewItemType
import com.appservice.recyclerView.AppBaseRecyclerViewItem
import com.framework.base.BaseResponse
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CatalogTileModel(

  @field:SerializedName("Tiles")
  var tiles: ArrayList<TilesItem>? = null,
) : BaseResponse()

data class TilesItem(

  @field:SerializedName("isEnabled")
  var isEnabled: Boolean? = null,

  @field:SerializedName("icon")
  var icon: String? = null,

  @field:SerializedName("description")
  var description: String? = null,

  @field:SerializedName("title")
  var title: String? = null,
  var recyclerViewItem: Int = RecyclerViewItemType.CATALOG_SETTING_TILES.getLayout()
) : Serializable, AppBaseRecyclerViewItem {
  override fun getViewType(): Int {
    return recyclerViewItem
  }
}

enum class IconType(var icon: Int) {
  policies(R.drawable.ic_policies_for_customer),
  customer_invoice_setup(R.drawable.ic_customer_invoice),
  payment_collection(R.drawable.ic_payment_collection),
  catalog_setup(R.drawable.ic_ecom_catalog_setup);

  companion object {
    fun fromName(name: String?): IconType? =
      values().firstOrNull { it.name.equals(name, ignoreCase = true) }
  }

}
