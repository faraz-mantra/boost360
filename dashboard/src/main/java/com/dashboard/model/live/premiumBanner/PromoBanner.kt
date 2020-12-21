package com.dashboard.model.live.premiumBanner

import com.dashboard.constant.RecyclerViewItemType
import com.dashboard.recyclerView.AppBaseRecyclerViewItem
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PromoBanner(
  @SerializedName("createdon")
  var createdon: String? = null,
  @SerializedName("cta_bundle_identifier")
  var ctaBundleIdentifier: String? = null,
  @SerializedName("cta_feature_key")
  var ctaFeatureKey: String? = null,
  @SerializedName("cta_web_link")
  var ctaWebLink: String? = null,
  @SerializedName("exclusive_to_categories")
  var exclusiveToCategories: ArrayList<String>? = null,
  @SerializedName("exclusive_to_customers")
  var exclusiveToCustomers: ArrayList<String>? = null,
  @SerializedName("image")
  var image: Image? = null,
  @SerializedName("isarchived")
  var isarchived: Boolean? = null,
  @SerializedName("_kid")
  var kid: String? = null,
  @SerializedName("_parentClassId")
  var parentClassId: String? = null,
  @SerializedName("_parentClassName")
  var parentClassName: String? = null,
  @SerializedName("_propertyName")
  var propertyName: String? = null,
  @SerializedName("title")
  var title: String? = null,
  @SerializedName("updatedon")
  var updatedon: String? = null,
  @SerializedName("websiteid")
  var websiteid: String? = null
): Serializable, AppBaseRecyclerViewItem{

  var recyclerViewItemType: Int = RecyclerViewItemType.BOOST_PREMIUM_ITEM_VIEW.getLayout()
  override fun getViewType(): Int {
    return recyclerViewItemType
  }
}