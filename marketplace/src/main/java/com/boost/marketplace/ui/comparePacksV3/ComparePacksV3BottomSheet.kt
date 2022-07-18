package com.boost.marketplace.ui.comparePacksV3

import android.view.View
import com.boost.dbcenterapi.data.api_model.GetAllFeatures.response.Bundles
import com.boost.marketplace.R
import com.boost.marketplace.databinding.Comparepacksv3PopupBinding
import com.bumptech.glide.Glide
import com.framework.base.BaseBottomSheetDialog
import com.framework.models.BaseViewModel
import com.framework.utils.RootUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ComparePacksV3BottomSheet: BaseBottomSheetDialog<Comparepacksv3PopupBinding, BaseViewModel>() {

    lateinit var bundleData: Bundles
    var offeredBundlePrice :Double = 0.0
    var originalBundlePrice :Double = 0.0
    var addonsSize :Int = 0

    override fun getLayout(): Int {
        return R.layout.comparepacksv3_popup
    }

    override fun getViewModelClass(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun onCreateView() {

        dialog.behavior.isDraggable = true

        bundleData = Gson().fromJson<Bundles>(
            requireArguments().getString("bundleData"),
            object : TypeToken<Bundles>() {}.type
        )

        addonsSize = requireArguments().getInt("addons")
        offeredBundlePrice = requireArguments().getDouble("price")
        originalBundlePrice = requireArguments().getDouble("price")

        binding?.packageTitle?.text=bundleData.name

        binding?.packageProfileImage?.let {
            Glide.with(this).load(bundleData.primary_image!!.url)
                .into(it)
        }

        if (bundleData.overall_discount_percent > 0) {
            offeredBundlePrice = RootUtil.round(originalBundlePrice - (originalBundlePrice * bundleData.overall_discount_percent / 100.0), 2)
            binding?.packDiscountTv?.visibility = View.VISIBLE
            binding?.packDiscountTv?.setText(bundleData.overall_discount_percent.toString() + "% SAVING")
        } else {
            offeredBundlePrice = originalBundlePrice
            binding?.packDiscountTv?.visibility = View.VISIBLE
        }

        binding?.buyPack?.text = "Buy " + bundleData.name

        binding?.addonsCountTv?.text= addonsSize.toString()+" PREMIUM FEATURES ideal for small businesses that want to get started with online sales."

        binding?.closeBtn?.setOnClickListener {
            dismiss()
        }
    }


}