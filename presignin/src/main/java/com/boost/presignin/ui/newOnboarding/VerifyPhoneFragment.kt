package com.boost.presignin.ui.newOnboarding

import android.os.Bundle
import android.os.Handler
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import com.boost.presignin.R
import com.boost.presignin.constant.FragmentType
import com.boost.presignin.constant.IntentConstant
import com.boost.presignin.databinding.FragmentVerifyPhoneBinding
import com.boost.presignin.helper.WebEngageController
import com.boost.presignin.model.authToken.AuthTokenDataItem
import com.boost.presignin.model.login.VerificationRequestResult
import com.boost.presignin.model.login.VerifyOtpResponse
import com.boost.presignin.ui.mobileVerification.AuthBaseFragment
import com.boost.presignin.ui.mobileVerification.FP_LIST_FRAGMENT
import com.boost.presignin.ui.mobileVerification.MobileVerificationActivity
import com.boost.presignin.ui.newOnboarding.bottomSheet.NeedHelpBottomSheet
import com.boost.presignin.views.otptextview.OTPListener
import com.framework.base.FRAGMENT_TYPE
import com.framework.extensions.gone
import com.framework.extensions.observeOnce
import com.framework.extensions.visible
import com.framework.pref.UserSessionManager
import com.framework.pref.clientId
import com.framework.pref.clientId2
import com.framework.smsVerification.SMSReceiver
import com.framework.smsVerification.SmsManager
import com.framework.webengageconstant.*

class VerifyPhoneFragment : AuthBaseFragment<FragmentVerifyPhoneBinding>(), SMSReceiver.OTPReceiveListener {

  private lateinit var countDown: com.boost.presignin.timer.CountDownTimer
  private var resultLogin: VerificationRequestResult? = null
  private var loginId: String? = null
  private var isCounterRunning = false

  private val phoneNumber by lazy {
    arguments?.getString(IntentConstant.EXTRA_PHONE_NUMBER.name)
  }

  override fun resultLogin(): VerificationRequestResult? {
    return resultLogin
  }

  override fun authTokenData(): AuthTokenDataItem? {
    return resultLogin()?.authTokens?.firstOrNull()
  }

  companion object {
    @JvmStatic
    fun newInstance(bundle: Bundle? = null): VerifyPhoneFragment {
      val fragment = VerifyPhoneFragment()
      fragment.arguments = bundle
      return fragment
    }
  }

  override fun getLayout(): Int {
    return R.layout.fragment_verify_phone
  }


  override fun onCreateView() {
    this.session = UserSessionManager(baseActivity)
    setOnListeners()
    setOnClickListener(binding?.tvVerifyOtp, binding?.tvResendOtpIn)
    WebEngageController.trackEvent(PS_VERIFY_OTP_PAGE_LOAD, PAGE_VIEW, NO_EVENT_VALUE)
    binding?.tvPhoneNumber?.text = phoneNumber.toString()
    Handler().postDelayed({ onCodeSent() }, 500)
    // init SMS Manager
    SmsManager.initManager(baseActivity, this)
  }

  override fun onClick(v: View) {
    super.onClick(v)
    when (v) {
      binding?.tvVerifyOtp -> verify()
      binding?.tvResendOtpIn -> {
        binding?.pinOtpVerify?.setOTP("")
        sendOtp(phoneNumber)
      }
    }
  }

  override fun onResume() {
    super.onResume()
    if (this::countDown.isInitialized) countDown.resume()
    SmsManager.register()
  }

  override fun onPause() {
    super.onPause()
    SmsManager.unregister()
  }

  private fun setOnListeners() {
    binding?.pinOtpVerify?.otpListener = object : OTPListener {
      override fun onInteractionListener() {
        val otp = binding?.pinOtpVerify?.otp
        binding?.tvVerifyOtp?.isEnabled = otp != null && otp.length == 4
      }

      override fun onOTPComplete(otp: String) {
        verify()
      }
    }

    binding?.chkWhatsapp?.setOnCheckedChangeListener { _, b ->
      if (b) {
        WebEngageController.trackEvent(Ps_Login_WhatsApp_Consent_click, CLICK, NO_EVENT_VALUE)
      } else {
        WebEngageController.trackEvent(Ps_Login_WhatsApp_Consent_not_clicked, CLICK, NO_EVENT_VALUE)
      }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    super.onCreateOptionsMenu(menu, inflater)
    inflater.inflate(R.menu.menu_help_on_boarding_new, menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.menu_item_help_onboard -> {
        NeedHelpBottomSheet().show(parentFragmentManager, NeedHelpBottomSheet::class.java.name)
        return true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun onCodeSent() {
    binding?.tvResendOtpIn?.setTextColor(getColor(R.color.gray_DADADA))
    countDown = object : com.boost.presignin.timer.CountDownTimer(30 * 1000, 1000) {
      override fun onTick(p0: Long) {
        binding?.tvResendOtpIn?.isEnabled = false
        val resendIn = getString(R.string.psn_resend_in);
        binding?.tvResendOtpIn?.text = String.format(resendIn, (p0 / 1000).toInt());
      }

      override fun onFinish() {
        binding?.tvResendOtpIn?.isEnabled = true
        isCounterRunning = false
        val clickableSpan = object : ClickableSpan() {
          override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = true
            ds.color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
            ds.linkColor = ContextCompat.getColor(requireContext(), R.color.colorAccent)
          }

          override fun onClick(widget: View) {
            view?.invalidate()
          }
        }
        val resendString = getString(R.string.psn_resend_hint)
        binding?.tvResendOtpIn?.text = resendString
        binding?.tvResendOtpIn?.setTextColor(getColor(R.color.colorAccent))
      }
    }
    startOperation()
  }

  private fun startOperation() {
    if (!isCounterRunning) {
      isCounterRunning = true
      countDown.start()
    } else {
      countDown.cancel() // cancel
      countDown.start() // then restart
    }
  }

  override fun onStop() {
    super.onStop()
    if (this::countDown.isInitialized) countDown.pause()
  }

  private fun sendOtp(phoneNumber: String?) {
    showProgress(getString(R.string.sending_otp))
    WebEngageController.trackEvent(PS_VERIFY_OTP_RESEND, OTP_RESEND_CLICK, NO_EVENT_VALUE)
    viewModel?.sendOtpIndia(phoneNumber?.toLong(), clientId)?.observeOnce(viewLifecycleOwner, {
      if (it.isSuccess() && it.parseResponse()) {
        onCodeSent()
      } else showShortToast(getString(R.string.otp_not_sent))
      hideProgress()
    })
  }

  fun verify() {
    showProgress(getString(R.string.verify_otp))
    WebEngageController.trackEvent(PS_VERIFY_OTP_VERIFY, OTP_VERIFY_CLICK, NO_EVENT_VALUE)
    val otp = binding?.pinOtpVerify?.otp
    viewModel?.verifyLoginOtp(number = phoneNumber, otp, clientId)?.observeOnce(viewLifecycleOwner, {
      if (it.isSuccess()) {
        val result = it as? VerifyOtpResponse
        binding?.tvResendOtpIn?.gone()
        if (result?.Result?.authTokens.isNullOrEmpty().not() && result?.Result?.authTokens?.size!! >= 1) {
          this.resultLogin = result.Result
          loginId = resultLogin?.loginId
          apiWhatsappOptin()
        } else {
          hideProgress()
          moveToWelcomeScreen(phoneNumber)
        }
      } else {
        hideProgress()
        showLongToast(getString(R.string.wrong_otp_tv))
      }
    })
  }

  private fun apiWhatsappOptin() {
    viewModel?.whatsappOptIn(0, phoneNumber, customerId = loginId)?.observeOnce(viewLifecycleOwner, {
      hideProgress()
      if (it.isSuccess()) {
        if (resultLogin()?.authTokens?.size == 1) {
          authTokenData()?.createAccessTokenAuth()
        } else {
          navigator?.startActivityFinish(
            MobileVerificationActivity::class.java,
            Bundle().apply {
              putInt(FRAGMENT_TYPE, FP_LIST_FRAGMENT);putSerializable(IntentConstant.EXTRA_FP_LIST_AUTH.name, resultLogin())
            })
        }
      } else showShortToast(it.message())
    })
  }

  private fun moveToWelcomeScreen(enteredPhone: String?) {
    startFragmentFromNewOnBoardingActivity(
      activity = baseActivity, type = FragmentType.WELCOME_FRAGMENT,
      bundle = Bundle().apply {
        putString(IntentConstant.EXTRA_PHONE_NUMBER.name, enteredPhone)
        putBoolean(IntentConstant.WHATSAPP_CONSENT_FLAG.name, false)
      },
      clearTop = false
    )
  }

  override fun onOTPReceived(otp: String?) {
    binding?.pinOtpVerify?.post { binding?.pinOtpVerify?.setOTP(otp ?: "") }
  }
}