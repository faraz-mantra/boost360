package com.festive.poster.ui.promoUpdates.bottomSheet

import android.os.Bundle
import com.festive.poster.R
import com.festive.poster.databinding.BsheetPostingProgressBinding
import com.festive.poster.models.PosterModel
import com.festive.poster.ui.promoUpdates.PostPreviewSocialActivity
import com.festive.poster.utils.SvgUtils
import com.framework.base.BaseBottomSheetDialog
import com.framework.models.BaseViewModel
import com.framework.utils.convertStringToObj
import com.framework.utils.loadUsingGlide
import com.google.gson.Gson
import java.io.File
import java.util.*
import kotlin.concurrent.schedule

class PostingProgressBottomSheet :
    BaseBottomSheetDialog<BsheetPostingProgressBinding, BaseViewModel>() {

    private var posterImgPath:String?=null
    companion object {

        val IK_POSTER="IK_POSTER"
        val IK_SOC_PARAMS="IK_SOC_PARAMS"

        @JvmStatic
        fun newInstance(posterImgPath: String?,socialParams:String): PostingProgressBottomSheet {
            val bundle = Bundle()
            bundle.putString(IK_POSTER,posterImgPath)
            bundle.putString(IK_SOC_PARAMS,socialParams)

            val fragment = PostingProgressBottomSheet()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getLayout(): Int {
        return R.layout.bsheet_posting_progress
    }

    override fun getViewModelClass(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun onCreateView() {
        posterImgPath = arguments?.getString(IK_POSTER)
        val socParams = arguments?.getString(IK_SOC_PARAMS)
        binding?.tvSocialParam?.text = socParams

        if (posterImgPath!=null){
            binding!!.ivPostIcon.loadUsingGlide(posterImgPath,false)
        }
    }
}