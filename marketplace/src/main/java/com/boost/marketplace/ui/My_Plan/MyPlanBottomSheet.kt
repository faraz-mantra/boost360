package com.boost.marketplace.ui.My_Plan

import android.content.Intent
import android.view.View
import android.widget.Toast
import com.boost.dbcenterapi.upgradeDB.model.FeaturesModel
import com.boost.marketplace.R
import com.boost.marketplace.databinding.BottomSheetMyplanBinding
import com.boost.marketplace.ui.DeepLink.Companion.getScreenType
import com.boost.marketplace.ui.details.FeatureDetailsActivity
import com.bumptech.glide.Glide
import com.framework.base.BaseBottomSheetDialog
import com.framework.models.BaseViewModel
import com.framework.pref.UserSessionManager
import com.framework.utils.DateUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import es.dmoral.toasty.Toasty


class MyPlanBottomSheet : BaseBottomSheetDialog<BottomSheetMyplanBinding, BaseViewModel>() {


    lateinit var singleAddon: FeaturesModel

    override fun getLayout(): Int {
        return R.layout.bottom_sheet_myplan
    }

    override fun getViewModelClass(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun onCreateView() {
        singleAddon = Gson().fromJson<FeaturesModel>(
            requireArguments().getString("bundleData"),
            object : TypeToken<FeaturesModel>() {}.type
        )
        binding?.addonsTitle?.text = singleAddon.name
        if (singleAddon.feature_code=="LIMITED_CONTENT" || singleAddon.feature_code=="UNLIMITED_CONTENT") {
            binding!!.btnUseThisFeature.visibility=View.GONE
        }
        binding?.addonsDesc?.text = singleAddon.description_title

        val date1: String? =
            DateUtils.parseDate(singleAddon.activatedDate, DateUtils.FORMAT_SERVER_DATE1, DateUtils.FORMAT1_DD_MM_YYYY)
        binding?.title3?.text= date1

        val date: String? =
            DateUtils.parseDate(singleAddon.expiryDate, DateUtils.FORMAT_SERVER_DATE1, DateUtils.FORMAT1_DD_MM_YYYY)
        binding?.title4?.text= date

        Glide.with(baseActivity).load(singleAddon.primary_image).into(binding!!.addonsIcon)
        if (singleAddon.status == 1) {
            binding!!.imageView3.setImageResource(R.drawable.ic_active)
            binding!!.actionRequired.visibility=View.GONE
            binding!!.actionText.visibility=View.GONE
        }
        else {
//            binding!!.imageView3.setImageResource(R.drawable.ic_action_req)
            binding!!.actionRequired.visibility=View.VISIBLE
            binding!!.actionText.visibility=View.VISIBLE
            binding!!.actionText.text="Please activate this feature by going to ${singleAddon.name} and provide the missing details."
            binding!!.txtMessage.visibility=View.GONE
            binding!!.paidSingleDummyView.visibility=View.GONE
            binding!!.imageView3.visibility=View.GONE
        }

        dialog.behavior.isDraggable = true
        setOnClickListener(
            binding?.btnUseThisFeature,
            binding?.btnFeatureDetails,
            binding?.rivCloseBottomSheet
        )
    }

    override fun onClick(v: View) {
        super.onClick(v)
        when (v) {
            binding?.btnUseThisFeature -> {
                Usefeature()
            }
            binding?.btnFeatureDetails -> {
                featuredetails()
            }
            binding?.rivCloseBottomSheet, binding?.rivCloseBottomSheet -> {
                dismiss()
            }
        }
    }

    private fun Usefeature() {
        val screenType= singleAddon.feature_code?.let { getScreenType(it) }
        if (screenType.isNullOrEmpty().not()){
        try {
           val intent = Intent(requireActivity(), Class.forName("com.dashboard.controller.DeepLinkTransActivity"))
           intent.putExtra("SCREEN_TYPE",screenType)
           startActivity(intent)
       } catch(e:Exception){
           e.printStackTrace()
       }
        }else{
            Toasty.error(requireContext(), "Coming Soon...", Toast.LENGTH_SHORT, true).show();
        }
    }

    private fun featuredetails() {
        val intent = Intent(requireActivity(), FeatureDetailsActivity::class.java)
        intent.putExtra("fpid", UserSessionManager(requireActivity()).fPID)
        intent.putExtra("expCode", UserSessionManager(requireActivity()).fP_AppExperienceCode)
        intent.putExtra(
            "accountType",
            UserSessionManager(requireActivity()).getFPDetails("GET_FP_DETAILS_CATEGORY")
        )
        intent.putStringArrayListExtra(
            "userPurchsedWidgets",
            ArrayList(UserSessionManager(requireActivity()).getStoreWidgets())
        )
        intent.putExtra("email", "ria@nowfloats.com")
        intent.putExtra("mobileNo", "9160004303")
        intent.putExtra("itemId", singleAddon.boost_widget_key)
        startActivity(intent)
    }
}