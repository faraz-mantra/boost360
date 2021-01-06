package com.appservice.constant

import androidx.annotation.LayoutRes
import com.appservice.R

enum class RecyclerViewItemType {
  PAGINATION_LOADER,
  SPECIFICATION_ITEM,
  IMAGE_PREVIEW,
  GST_DETAILS_VIEW,
  ADDITIONAL_FILE_VIEW,
  SERVICE_ITEM_VIEW,
  SESSION_ITEM_VIEW,
  STAFF_LISTING_VIEW,
  STAFF_FILTER_VIEW;

  @LayoutRes
  fun getLayout(): Int {
    return when (this) {
      PAGINATION_LOADER -> R.layout.pagination_loader
      SPECIFICATION_ITEM -> R.layout.row_layout_added_specs
      IMAGE_PREVIEW -> R.layout.item_preview_image
      GST_DETAILS_VIEW -> R.layout.item_gst_detail
      ADDITIONAL_FILE_VIEW -> R.layout.item_pdf_file
      SERVICE_ITEM_VIEW -> R.layout.recycler_item_service
      SESSION_ITEM_VIEW -> R.layout.recycler_item_session
      STAFF_LISTING_VIEW -> R.layout.recycler_item_staff_listing
      STAFF_FILTER_VIEW -> R.layout.recycler_item_staff_filter
    }
  }
}
