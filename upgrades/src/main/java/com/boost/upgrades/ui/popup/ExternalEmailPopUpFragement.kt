package com.boost.upgrades.ui.popup

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.boost.upgrades.R
import com.boost.upgrades.UpgradeActivity
import com.boost.upgrades.ui.payment.PaymentViewModel
import com.boost.upgrades.utils.Utils
import com.boost.upgrades.utils.WebEngageController
import com.razorpay.Razorpay
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.add_external_email_popup.*
import kotlinx.android.synthetic.main.add_upi_popup.upi_popup_outer_layout
import org.json.JSONObject

class ExternalEmailPopUpFragement : DialogFragment() {

    lateinit var root: View
    private lateinit var viewModel: PaymentViewModel

    lateinit var razorpay: Razorpay

    var validatingStatus = false

    override fun onStart() {
        super.onStart()
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog!!.window!!.setLayout(width, height)
        dialog!!.window!!.setBackgroundDrawableResource(R.color.fullscreen_color)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.add_external_email_popup, container, false)

        razorpay = (activity as UpgradeActivity).getRazorpayObject()

        return root

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity()).get(PaymentViewModel::class.java)

        external_email_popup_container_layout.setOnClickListener {
            dialog!!.dismiss()
        }
        external_email_popup_container_layout.setOnClickListener {}

        external_email_popup_submit.setOnClickListener {
            if(!validatingStatus) {
                validatingStatus = true
                Utils.hideSoftKeyboard(requireActivity())
                external_email_popup_submit.setText("Verifying...")
                Thread.sleep(1000L)
                if(validateEmail()){
                    validatingStatus = false
                    external_email_popup_submit.setText("CONFIRM")
                    WebEngageController.trackEvent("ADDONS_MARKETPLACE Ext_Email Validation Success", external_email_popup_value.text.toString(), "")
                    sendPaymentLinkEmail()
                    invalid_email.visibility = View.GONE
                } else {
                    validatingStatus = false
                    WebEngageController.trackEvent("ADDONS_MARKETPLACE Ext_Email Validation Failed", external_email_popup_value.text.toString(), "")
                    Toasty.warning(requireContext(),"Invalid Email Id. Please try again.",Toast.LENGTH_LONG).show()
                    invalid_email.visibility = View.VISIBLE
                }
            }
        }
        WebEngageController.trackEvent("ADDONS_MARKETPLACE ADD_UPI Loaded", "ADD_UPI", "")
    }

    fun validateEmail(): Boolean{
        return !TextUtils.isEmpty(external_email_popup_value.text.toString()) && android.util.Patterns.EMAIL_ADDRESS.matcher(external_email_popup_value.text.toString()).matches()
    }

    fun sendPaymentLinkEmail(){
        val data = JSONObject()
        data.put("method", "email")
        data.put("email", external_email_popup_value.text.toString())
        viewModel.UpdateExternalEmailPaymentData(data)
        dialog!!.dismiss()
        clearData()
    }

    fun clearData(){
        external_email_popup_value.text.clear()
    }
}