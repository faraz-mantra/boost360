package com.onboarding.nowfloats.ui.updateChannel.digitalChannel

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.viewpager2.widget.ViewPager2
import com.framework.base.BaseBottomSheetDialog
import com.framework.extensions.gone
import com.framework.extensions.observeOnce
import com.framework.extensions.visible
import com.framework.views.dotsindicator.OffsetPageTransformer
import com.onboarding.nowfloats.R
import com.onboarding.nowfloats.constant.RecyclerViewItemType
import com.onboarding.nowfloats.databinding.DialogDigitalCardShareBinding
import com.onboarding.nowfloats.extensions.capitalizeWords
import com.onboarding.nowfloats.model.digitalCard.DigitalCardData
import com.onboarding.nowfloats.model.profile.MerchantProfileResponse
import com.onboarding.nowfloats.model.profile.ProfileProperties
import com.onboarding.nowfloats.recyclerView.AppBaseRecyclerViewAdapter
import com.onboarding.nowfloats.recyclerView.BaseRecyclerViewItem
import com.onboarding.nowfloats.recyclerView.RecyclerItemClickListener
import com.onboarding.nowfloats.utils.viewToBitmap
import com.onboarding.nowfloats.viewmodel.channel.ChannelPlanViewModel

class MyDigitalCardShareDialog : BaseBottomSheetDialog<DialogDigitalCardShareBinding, ChannelPlanViewModel>(), RecyclerItemClickListener {

  private var cardPosition = 0
  private var localSessionModel: LocalSessionModel? = null

  fun setData(localSessionModel: LocalSessionModel) {
    this.localSessionModel = localSessionModel
  }

  override fun getLayout(): Int {
    return R.layout.dialog_digital_card_share
  }

  override fun getViewModelClass(): Class<ChannelPlanViewModel> {
    return ChannelPlanViewModel::class.java
  }

  override fun onCreateView() {
    binding?.progress?.visible()
    viewModel?.getMerchantProfile(localSessionModel?.floatingPoint)?.observeOnce(viewLifecycleOwner, {
      if (it.isSuccess()) {
        val response = it as? MerchantProfileResponse
        val userDetail = response?.result?.getUserDetail()
        localSessionModel?.contactName = when {
          localSessionModel?.contactName.isNullOrEmpty().not() -> localSessionModel?.contactName
          userDetail?.userName.isNullOrEmpty().not() -> userDetail?.userName
          else -> localSessionModel?.fpTag
        }
        localSessionModel?.primaryNumber = if (localSessionModel?.primaryNumber.isNullOrEmpty().not()) localSessionModel?.primaryNumber else if (userDetail?.userMobile.isNullOrEmpty().not()) userDetail?.userMobile else ""
        localSessionModel?.primaryEmail = if (localSessionModel?.primaryEmail.isNullOrEmpty().not()) localSessionModel?.primaryEmail else if (userDetail?.userEmail.isNullOrEmpty().not()) userDetail?.userEmail else ""

        val userProfile = ProfileProperties(userName = localSessionModel?.contactName, userMobile = localSessionModel?.primaryNumber, userEmail = localSessionModel?.primaryEmail)
        val cardList = ArrayList<DigitalCardData>()
        cardList.add(DigitalCardData(localSessionModel?.businessName, localSessionModel?.businessImage, localSessionModel?.location, userProfile.userName?.capitalizeWords(),
            userProfile.userMobile, userProfile.userEmail, localSessionModel?.businessType, localSessionModel?.websiteUrl,recyclerViewType = RecyclerViewItemType.VISITING_CARD_ONE_ITEM.getLayout()))
        cardList.add(DigitalCardData(localSessionModel?.businessName, localSessionModel?.businessImage, localSessionModel?.location, userProfile.userName?.capitalizeWords(),
            userProfile.userMobile, userProfile.userEmail, localSessionModel?.businessType, localSessionModel?.websiteUrl,recyclerViewType = RecyclerViewItemType.VISITING_CARD_TWO_ITEM.getLayout()))
        cardList.add(DigitalCardData(localSessionModel?.businessName, localSessionModel?.businessImage, localSessionModel?.location, userProfile.userName?.capitalizeWords(),
            userProfile.userMobile, userProfile.userEmail, localSessionModel?.businessType, localSessionModel?.websiteUrl,recyclerViewType = RecyclerViewItemType.VISITING_CARD_THREE_ITEM.getLayout()))
        cardList.add(DigitalCardData(localSessionModel?.businessName, localSessionModel?.businessImage, localSessionModel?.location, userProfile.userName?.capitalizeWords(),
            userProfile.userMobile, userProfile.userEmail, localSessionModel?.businessType, localSessionModel?.websiteUrl,recyclerViewType = RecyclerViewItemType.VISITING_CARD_FOUR_ITEM.getLayout()))
        cardList.add(DigitalCardData(localSessionModel?.businessName, localSessionModel?.businessImage, localSessionModel?.location, userProfile.userName?.capitalizeWords(),
            userProfile.userMobile, userProfile.userEmail, localSessionModel?.businessType, localSessionModel?.websiteUrl,recyclerViewType = RecyclerViewItemType.VISITING_CARD_FIVE_ITEM.getLayout()))

        setAdapterCard(cardList)
      } else {
        showShortToast(it.message())
        dismiss()
      }
      binding?.progress?.gone()
//      binding?.shareCard?.visible()
    })
    binding?.backBtn?.setOnClickListener { dismiss() }
//    binding?.shareCard?.setOnClickListener { shareCardWhatsApp("Business Card",isWhatsApp)  }
  }

  private fun setAdapterCard(cardList: ArrayList<DigitalCardData>) {
    binding?.pagerDigitalCard?.apply {
      val adapterPager3 = AppBaseRecyclerViewAdapter(baseActivity, cardList, this@MyDigitalCardShareDialog)
      offscreenPageLimit = 3
      isUserInputEnabled = true
      adapter = adapterPager3
      binding?.dotIndicatorCard?.setViewPager2(this)
      setPageTransformer { page, position -> OffsetPageTransformer().transformPage(page, position) }
      registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
          super.onPageSelected(position)
          cardPosition = position
        }
      })
    }
  }

  private fun shareCardWhatsApp(messageN: String?, isWhatsApp: Boolean) {
    if (ActivityCompat.checkSelfPermission(baseActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
      ActivityCompat.requestPermissions(baseActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
      return
    }
    binding?.progress?.visible()
    val bitmap = binding?.pagerDigitalCard?.getChildAt(0)?.let { viewToBitmap(it) }
    val pm = baseActivity.packageManager
    try {
      val cropBitmap = bitmap?.let { Bitmap.createBitmap(it, 33, 0, bitmap.width - 66, bitmap.height) }
      val path = MediaStore.Images.Media.insertImage(baseActivity.contentResolver, cropBitmap, "boost_360", null)
      val imageUri: Uri = Uri.parse(path)
      val waIntent = Intent(Intent.ACTION_SEND)
      waIntent.type = "image/*"
      if (isWhatsApp) waIntent.setPackage("com.whatsapp")
      waIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
      waIntent.putExtra(Intent.EXTRA_TEXT, messageN ?: "")
      baseActivity.startActivity(Intent.createChooser(waIntent, "Share your business card..."))
      binding?.progress?.gone()
      dismiss()
    } catch (e: Exception) {
      showLongToast("App not Installed")
      binding?.progress?.gone()
      dismiss()
    }
  }

  override fun onItemClick(position: Int, item: BaseRecyclerViewItem?, actionType: Int) {

  }
}

data class LocalSessionModel(
    var floatingPoint: String? = null,
    var contactName: String? = null,
    var businessName: String? = null,
    var businessImage: String? = null,
    var location: String? = null,
    var websiteUrl: String? = null,
    var businessType: String? = null,
    var primaryNumber: String? = null,
    var primaryEmail: String? = null,
    var fpTag: String? = null,
)