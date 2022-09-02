package com.boost.marketplace.ui.popup.customdomains

import com.boost.marketplace.R
import com.boost.marketplace.databinding.PopupCallExpertCustomDomainBinding
import com.framework.base.BaseBottomSheetDialog
import com.framework.models.BaseViewModel

class CustomDomainHelpBottomSheet : BaseBottomSheetDialog<PopupCallExpertCustomDomainBinding, BaseViewModel>() {


    override fun getLayout(): Int {
        return R.layout.popup_call_expert_custom_domain
    }

    override fun getViewModelClass(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun onCreateView() {

        binding?.backBtn?.setOnClickListener {
            dismiss()
        }
    }

}