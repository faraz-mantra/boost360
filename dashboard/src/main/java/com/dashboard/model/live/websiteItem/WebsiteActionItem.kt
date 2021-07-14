package com.dashboard.model.live.websiteItem

import com.dashboard.R
import com.dashboard.constant.RecyclerViewItemType
import com.dashboard.recyclerView.AppBaseRecyclerViewItem
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class WebsiteActionItem(
  @SerializedName("isLock")
  var isLock: Boolean? = false,
  @SerializedName("premiumCode")
  var premiumCode: String? = "",
  @SerializedName("title")
  var title: String? = "",
  @SerializedName("desc")
  var desc: String? = "",
  @SerializedName("type")
  var type: String? = "",
) : Serializable, AppBaseRecyclerViewItem {

  var recyclerViewItemType: Int = RecyclerViewItemType.BOOST_WEBSITE_ITEM_VIEW.getLayout()
  override fun getViewType(): Int {
    return recyclerViewItemType
  }

  enum class IconType(var icon: Int) {
    service_product_catalogue(R.drawable.ic_product_cataloge_d),
    latest_update_tips(R.drawable.ic_daily_business_update_d),
    all_images(R.drawable.picture_gallery),
    business_profile(R.drawable.ic_customer_enquiries_d),
    testimonials(R.drawable.ic_customer_testimonial_d),
    custom_page(R.drawable.ic_custom_page_add),
    project_teams(R.drawable.ic_project_teams_d),
    unlimited_digital_brochures(R.drawable.ic_digital_brochures_d),
    toppers_institute(R.drawable.toppers_institute_d),
    upcoming_batches(R.drawable.ic_upcoming_batch_d),
    faculty_management(R.drawable.ic_upcoming_batch_d),
    places_look_around(R.drawable.places_look_around_d),
    trip_adviser_ratings(R.drawable.trip_advisor_reviews_d),
    seasonal_offers(R.drawable.ic_offer_d),
    website_theme(R.drawable.ic_website_theme);

    companion object {
      fun fromName(name: String?): IconType? =
        values().firstOrNull { it.name.equals(name, ignoreCase = true) }
    }
  }
}