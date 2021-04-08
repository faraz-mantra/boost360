package com.boost.upgrades.ui.razorpay

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.boost.upgrades.R
import com.boost.upgrades.UpgradeActivity
import com.boost.upgrades.data.api_model.Razorpay.PaymentErrorModule
import com.boost.upgrades.ui.confirmation.OrderConfirmationFragment
import com.boost.upgrades.ui.payment.PaymentViewModel
import com.boost.upgrades.ui.popup.FailedTransactionPopUpFragment
import com.boost.upgrades.utils.Constants
import com.boost.upgrades.utils.SharedPrefs
import com.boost.upgrades.utils.WebEngageController
import com.framework.webengageconstant.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.razorpay.PaymentResultListener
import com.razorpay.Razorpay
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.razor_pay_web_view_fragment.*
import org.json.JSONObject


class RazorPayWebView : DialogFragment() {

    lateinit var root: View

    lateinit var razorpay: Razorpay

    val failedTransactionPopUpFragment = FailedTransactionPopUpFragment()

    var data = JSONObject()

    companion object {
        fun newInstance() = RazorPayWebView()
    }

    private lateinit var viewModel: PaymentViewModel

    override fun onStart() {
        super.onStart()
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog!!.window!!.setLayout(width, height)
        dialog!!.window!!.setBackgroundDrawableResource(R.color.fullscreen_color)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.razor_pay_web_view_fragment, container, false)

        razorpay = (activity as UpgradeActivity).getRazorpayObject()

        val jsonString = arguments!!.getString("data")
        data = JSONObject(jsonString!!)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity()).get(PaymentViewModel::class.java)

        if(savedInstanceState == null ) {

            try {
                // Make webview visible before submitting payment details
                razorpay.setWebView(razorpay_webview);
                razorpay.submit(data, object : PaymentResultListener {

                    override fun onPaymentSuccess(razorpayPaymentId: String) {
                        // Razorpay payment ID is passed here after a successful payment
                        Log.i("onPaymentSuccess", razorpayPaymentId)
                        val revenue = data["amount"] as Int
                        WebEngageController.trackEvent("ADDONS_MARKETPLACE Payment Success",
                                "rev", (revenue / 100).toString())

                        var firebaseAnalytics = Firebase.analytics
                        val bundle = Bundle()
                        bundle.putDouble(FirebaseAnalytics.Param.VALUE, (revenue / 100).toDouble())
                        bundle.putString(FirebaseAnalytics.Param.TRANSACTION_ID, razorpayPaymentId)
                        bundle.putString(FirebaseAnalytics.Param.CURRENCY, "INR")
                        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE, bundle)

                        redirectOrderConfirmation(razorpayPaymentId)
                        dialog!!.dismiss()
                    }

                    override fun onPaymentError(p0: Int, p1: String?) {
                        // Error code and description is passed here
                        Log.e("onPaymentError", "p1 >>>" + p1)
                        WebEngageController.trackEvent(ADDONS_MARKETPLACE_FAILED_PAYMENT_TRANSACTION_LOAD, ADDONS_MARKETPLACE, NO_EVENT_VALUE)
                        val listPersonType = object : TypeToken<PaymentErrorModule>() {}.type
                        val errorBody: PaymentErrorModule = Gson().fromJson(p1, listPersonType)
                        Toasty.error(requireContext(), errorBody.error.description, Toast.LENGTH_LONG).show()
                        WebEngageController.trackEvent(ADDONS_MARKETPLACE_PAYMENT_FAILED, NO_EVENT_LABLE, NO_EVENT_VALUE)
                        redirectTransactionFailure(data.toString())
                        dialog!!.dismiss()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }

            WebEngageController.trackEvent(ADDONS_MARKETPLACE_RAZOR_PAY_VIEW_LOADED, RAZOR_PAY_VIEW, NO_EVENT_VALUE)
        }
    }



    fun redirectOrderConfirmation(paymentTransactionId: String) {
        var prefs = SharedPrefs(activity as UpgradeActivity)
        prefs.storeLatestOrderStatus(1)

        //RAZORPAY payment ID IS NOT BEEN USED  ---> renamed to prefs.storeTransactionIdFromCart()
//        prefs.storeLatestPaymentIdFromPG(paymentTransactionId)

        (activity as UpgradeActivity).replaceFragment(
            OrderConfirmationFragment.newInstance(),
            Constants.ORDER_CONFIRMATION_FRAGMENT
        )
    }

    fun redirectTransactionFailure(data: String){
        val args = Bundle()
        args.putString("data", data)
        failedTransactionPopUpFragment.arguments = args
        failedTransactionPopUpFragment.show((activity as UpgradeActivity).supportFragmentManager,
            Constants.FAILED_TRANSACTION_FRAGMENT
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        razorpay.reset()
    }

}
