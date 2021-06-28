package com.appservice.ui.catalog.catalogService.listing

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.appservice.R
import com.appservice.base.AppBaseFragment
import com.appservice.constant.FragmentType
import com.appservice.constant.IntentConstant
import com.appservice.constant.RecyclerViewActionType
import com.appservice.databinding.FragmentServiceListingBinding
import com.appservice.recyclerView.AppBaseRecyclerViewAdapter
import com.appservice.recyclerView.BaseRecyclerViewItem
import com.appservice.recyclerView.PaginationScrollListener
import com.appservice.recyclerView.PaginationScrollListener.Companion.PAGE_SIZE
import com.appservice.recyclerView.PaginationScrollListener.Companion.PAGE_START
import com.appservice.recyclerView.RecyclerItemClickListener
import com.appservice.ui.catalog.startFragmentActivity
import com.appservice.ui.catalog.widgets.ImagePickerBottomSheet
import com.appservice.ui.model.*
import com.appservice.utils.WebEngageController
import com.appservice.viewmodel.ServiceViewModel
import com.framework.extensions.gone
import com.framework.extensions.observeOnce
import com.framework.extensions.visible
import com.framework.models.firestore.FirestoreManager
import com.framework.pref.UserSessionManager
import com.framework.pref.getDomainName
import com.framework.utils.ContentSharing
import com.framework.webengageconstant.*
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.fragment_add_account_start.view.*
import kotlinx.android.synthetic.main.fragment_service_detail.*
import kotlinx.android.synthetic.main.recycler_item_service_timing.*
import java.util.*

class ServiceListingFragment : AppBaseFragment<FragmentServiceListingBinding, ServiceViewModel>(),
    RecyclerItemClickListener {

    private lateinit var domainName: String
    private var session: UserSessionManager? = null
    private val list: ArrayList<ItemsItem> = arrayListOf()
    private val finalList: ArrayList<ItemsItem> = arrayListOf()
    private var adapterService: AppBaseRecyclerViewAdapter<ItemsItem>? = null
    private var targetMap: Target? = null
    private var isNonPhysicalExperience: Boolean? = null
    private var currencyType: String? = "INR"
    private var fpId: String? = null
    private var fpTag: String? = null
    private var clientId: String? = null
    private var externalSourceId: String? = null
    private var applicationId: String? = null
    private var userProfileId: String? = null
    private var layoutManagerN: LinearLayoutManager? = null

    /* Paging */
    private var isLoadingD = false
    private var TOTAL_ELEMENTS = 0
    private var offSet: Int = PAGE_START
    private var limit: Int = PAGE_SIZE + 1
    private var isLastPageD = false

    companion object {
        fun newInstance(): ServiceListingFragment {
            return ServiceListingFragment()
        }
  companion object {
    fun newInstance(isNonPhysicalExperience: Boolean?, currencyType: String?, fpId: String?, fpTag: String?, clientId: String?, externalSourceId: String?, applicationId: String?, userProfileId: String?): ServiceListingFragment {
      val bundle = Bundle()
      bundle.putBoolean(IntentConstant.NON_PHYSICAL_EXP_CODE.name, isNonPhysicalExperience!!)
      bundle.putString(IntentConstant.CURRENCY_TYPE.name, "INR")
      bundle.putString(IntentConstant.FP_ID.name, fpId)
      bundle.putString(IntentConstant.FP_TAG.name, fpTag)
      bundle.putString(IntentConstant.USER_PROFILE_ID.name, userProfileId)
      bundle.putString(IntentConstant.CLIENT_ID.name, clientId)
      bundle.putString(IntentConstant.EXTERNAL_SOURCE_ID.name, externalSourceId)
      bundle.putString(IntentConstant.APPLICATION_ID.name, applicationId)
      val serviceListingFragment = ServiceListingFragment()
      serviceListingFragment.arguments = bundle
      return serviceListingFragment
    }

        private const val STORAGE_CODE = 120
        var shareType = false
        var shareProduct: ItemsItem? = null
    }
    private const val STORAGE_CODE = 120
    var shareType = 0
    var shareProduct: ItemsItem? = null
    fun newInstance(): ServiceListingFragment {
      return ServiceListingFragment()
    }
  }


    override fun getLayout(): Int {
        return R.layout.fragment_service_listing
    }


    override fun getViewModelClass(): Class<ServiceViewModel> {
        return ServiceViewModel::class.java
    }

    override fun onCreateView() {
        super.onCreateView()
        getBundleData()
        layoutManagerN = LinearLayoutManager(baseActivity)
        getListServiceFilterApi(isFirst = true, offSet = offSet, limit = limit)
        WebEngageController.trackEvent(SERVICE_CATALOGUE_LIST, PAGE_VIEW, NO_EVENT_VALUE)
        layoutManagerN?.let { scrollPagingListener(it) }
        setOnClickListener(binding?.cbAddService, binding?.serviceListingEmpty?.cbAddService)
        this.session = UserSessionManager(requireContext())
        this.domainName = session?.getDomainName()!!

    }

    private fun getBundleData() {
        isNonPhysicalExperience = arguments?.getBoolean(IntentConstant.NON_PHYSICAL_EXP_CODE.name)
        currencyType = arguments?.getString(IntentConstant.CURRENCY_TYPE.name) ?: "INR"
        fpId = arguments?.getString(IntentConstant.FP_ID.name)
        fpTag = arguments?.getString(IntentConstant.FP_TAG.name)
        clientId = arguments?.getString(IntentConstant.CLIENT_ID.name)
        externalSourceId = arguments?.getString(IntentConstant.EXTERNAL_SOURCE_ID.name)
        applicationId = arguments?.getString(IntentConstant.APPLICATION_ID.name)
        userProfileId = arguments?.getString(IntentConstant.USER_PROFILE_ID.name)
    }


    private fun scrollPagingListener(layoutManager: LinearLayoutManager) {
        binding?.baseRecyclerView?.addOnScrollListener(object :
            PaginationScrollListener(layoutManager) {
            override fun loadMoreItems() {
                if (!isLastPageD) {
                    isLoadingD = true
                    adapterService?.addLoadingFooter(ItemsItem().getLoaderItem())
                    offSet += limit
                    getListServiceFilterApi(offSet = offSet, limit = limit)
                }
            }

            override val totalPageCount: Int
                get() = TOTAL_ELEMENTS
            override val isLastPage: Boolean
                get() = isLastPageD
            override val isLoading: Boolean
                get() = isLoadingD
        })
    }

    private fun getListServiceFilterApi(
        searchString: String = "",
        isFirst: Boolean = false,
        offSet: Int? = null,
        limit: Int? = null
    ) {
        if (isFirst || searchString.isNotEmpty()) showProgress()
        viewModel?.getSearchListings(fpTag, fpId, searchString, offSet, limit)
            ?.observeOnce(viewLifecycleOwner, {
                if (it.isSuccess()) {
                    setServiceDataItems(
                        (it as? ServiceSearchListingResponse)?.result,
                        searchString.isNotEmpty(),
                        isFirst
                    )
                } else if (isFirst) showShortToast(it.message())
                if (isFirst || searchString.isNotEmpty()) hideProgress()
            })
    }

    private fun setServiceDataItems(
        resultService: Result?,
        isSearchString: Boolean,
        isFirstLoad: Boolean
    ) {
        val listService = resultService?.data as? ArrayList<ItemsItem>
        if (isSearchString.not()) {
            onServiceAddedOrUpdated(listService?.size ?: 0)
            if (isFirstLoad) finalList.clear()
            if (listService.isNullOrEmpty().not()) {
                removeLoader()
                setEmptyView(View.GONE)
                TOTAL_ELEMENTS = resultService?.paging?.count ?: 0
                finalList.addAll(listService!!)
                list.clear()
                list.addAll(finalList)
                isLastPageD = (finalList.size == TOTAL_ELEMENTS)
                setAdapterNotify()
                setToolbarTitle("${resources.getString(R.string.services)} (${TOTAL_ELEMENTS})")
            } else if (isFirstLoad) setEmptyView(View.VISIBLE)
        } else {
            if (listService.isNullOrEmpty().not()) {
                list.clear()
                list.addAll(listService!!)
                setAdapterNotify()
            }
        }
    }
  private fun setServiceDataItems(resultService: Result?, isSearchString: Boolean, isFirstLoad: Boolean) {
    val listService = resultService?.data as? ArrayList<ItemsItem>
    if (isSearchString.not()) {
      onServiceAddedOrUpdated(listService?.size ?: 0)
      if (isFirstLoad) finalList.clear()
      if (listService.isNullOrEmpty().not()) {
        removeLoader()
        setEmptyView(View.GONE)
        TOTAL_ELEMENTS = resultService?.paging?.count ?: 0
        finalList.addAll(listService!!)
        list.clear()
        list.addAll(finalList)
        isLastPageD = (finalList.size == TOTAL_ELEMENTS)
        setAdapterNotify()
//        setToolbarTitle("${resources.getString(R.string.services)} (${TOTAL_ELEMENTS})")
      } else if (isFirstLoad) setEmptyView(View.VISIBLE)
    } else {
      if (listService.isNullOrEmpty().not()) {
        list.clear()
        list.addAll(listService!!)
        setAdapterNotify()
      }
    }
  }

    private fun onServiceAddedOrUpdated(count: Int) {
        val instance = FirestoreManager
        if (instance.getDrScoreData()?.metricdetail == null) return
        instance.getDrScoreData()?.metricdetail?.number_services_added = count
        instance.updateDocument()
    }

    private fun setAdapterNotify() {
        if (adapterService == null) {
            adapterService =
                AppBaseRecyclerViewAdapter(baseActivity, list, this@ServiceListingFragment)
            binding?.baseRecyclerView?.layoutManager = layoutManagerN
            binding?.baseRecyclerView?.adapter = adapterService
            adapterService?.runLayoutAnimation(binding?.baseRecyclerView)
        } else adapterService?.notifyDataSetChanged()
    }

    private fun removeLoader() {
        if (isLoadingD) {
            isLoadingD = false
            adapterService?.removeLoadingFooter()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_service_listing, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as? SearchView
        val searchAutoComplete =
            searchView?.findViewById<SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)
        val searchCloseIcon =
            searchView?.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        searchCloseIcon?.setColorFilter(
            resources.getColor(R.color.white),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        searchAutoComplete?.setHintTextColor(getColor(R.color.white_70))
        searchAutoComplete?.setTextColor(getColor(R.color.white))
        searchView?.setIconifiedByDefault(true)
        searchView?.queryHint = getString(R.string.search_services)
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                startFilter(newText)
                return false
            }
        })
    }

    private fun startFilter(query: String?) {
        when {
            query.isNullOrEmpty().not() && query!!.length > 2 -> getListServiceFilterApi(
                searchString = query
            )
            finalList.isNullOrEmpty().not() -> {
                list.clear()
                list.addAll(finalList)
                setAdapterNotify()
            }
            else -> setEmptyView(View.VISIBLE)
        }
    }
  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.action_service_configuration -> {
        startFragmentActivity(FragmentType.APPOINTMENT_SETTINGS)
        return true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun startFilter(query: String?) {
    when {
      query.isNullOrEmpty().not() && query!!.length > 2 -> getListServiceFilterApi(searchString = query)
      finalList.isNullOrEmpty().not() -> {
        list.clear()
        list.addAll(finalList)
        setAdapterNotify()
      }
      else -> setEmptyView(View.VISIBLE)
    }
  }

    private fun setEmptyView(visibility: Int) {
        binding?.serviceListingEmpty?.root?.visibility = visibility
        if (visibility == View.VISIBLE) setListingView(View.GONE) else setListingView(View.VISIBLE)
        setEmptyView()
    }

    private fun setListingView(visibility: Int) {
        binding?.baseRecyclerView?.visibility = visibility
        binding?.llActionButtons?.visibility = visibility
    }

    private fun setEmptyView() {
        val spannableString =
            SpannableString(resources.getString(R.string.you_don_t_have_any_service_added_to_your_digital_catalog_as_of_yet_watch_video))
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                showShortToast("video link")
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }
        spannableString.setSpan(
            clickableSpan,
            spannableString.length.minus(11),
            spannableString.length,
            0
        )
        spannableString.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.black_4a4a4a
                )
            ), spannableString.length.minus(11), spannableString.length, 0
        )
        spannableString.setSpan(
            UnderlineSpan(),
            spannableString.length.minus(11),
            spannableString.length,
            0
        )
        binding?.serviceListingEmpty?.ctvAddServiceSubheading?.text = spannableString
        binding?.serviceListingEmpty?.ctvAddServiceSubheading?.movementMethod =
            LinkMovementMethod.getInstance()
        binding?.serviceListingEmpty?.ctvAddServiceSubheading?.highlightColor =
            resources.getColor(android.R.color.transparent)
    }

    override fun onItemClick(position: Int, item: BaseRecyclerViewItem?, actionType: Int) {
        if (actionType == RecyclerViewActionType.SERVICE_ITEM_CLICK.ordinal) {
            startFragmentActivity(
                FragmentType.SERVICE_DETAIL_VIEW,
                sendBundleData(item as? ItemsItem),
                false,
                isResult = true
            )
        }
        if (actionType == RecyclerViewActionType.SERVICE_WHATS_APP_SHARE.ordinal) {
            shareProduct = item as? ItemsItem
            shareType = true
            if (checkStoragePermission()) ContentSharing.shareProduct(

                shareProduct?.name,
                shareProduct?.price.toString(),
                "${domainName}/all-services",
                session?.fPPrimaryContactNumber,
                shareProduct?.image,
                true,
                isService = true,
               activity = requireActivity()
            )
        }
        if (actionType == RecyclerViewActionType.SERVICE_DATA_SHARE_CLICK.ordinal) {
            shareProduct = item as? ItemsItem
            shareType = false
            if (checkStoragePermission()) ContentSharing.shareProduct(
                shareProduct?.name,
                shareProduct?.price.toString(),
                "${domainName}/all-services",
                session?.fPPrimaryContactNumber,
                shareProduct?.image,
                isService = true,
                activity = requireActivity()

            )
        }
    }

    private fun sendBundleData(itemsItem: ItemsItem?): Bundle {
        val bundle = Bundle()
        bundle.putSerializable(IntentConstant.PRODUCT_DATA.name, itemsItem)
        bundle.putBoolean(
            IntentConstant.NON_PHYSICAL_EXP_CODE.name,
            isNonPhysicalExperience ?: false
        )
        bundle.putString(IntentConstant.CURRENCY_TYPE.name, currencyType)
        bundle.putString(IntentConstant.FP_ID.name, fpId)
        bundle.putString(IntentConstant.FP_TAG.name, fpTag)
        bundle.putString(IntentConstant.USER_PROFILE_ID.name, userProfileId)
        bundle.putString(IntentConstant.CLIENT_ID.name, clientId)
        bundle.putString(IntentConstant.EXTERNAL_SOURCE_ID.name, externalSourceId)
        bundle.putString(IntentConstant.APPLICATION_ID.name, applicationId)
        return bundle
    }

    private fun checkStoragePermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED
        ) {
            showDialog(
                requireActivity(),
                "Storage Permission",
                "To share the image we need storage permission."
            ) { _: DialogInterface?, _: Int ->
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_CODE
                )
            }
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (shareProduct != null) ContentSharing.shareProduct(
                shareProduct?.name,
                shareProduct?.price.toString(),
                imageUri = shareProduct?.image,
                isWhatsApp = shareType,
                activity = requireActivity()

            )
        }
    }

    fun showDialog(
        mContext: Context?,
        title: String?,
        msg: String?,
        listener: DialogInterface.OnClickListener
    ) {
        val builder = AlertDialog.Builder(mContext!!)
        builder.setTitle(title).setMessage(msg).setPositiveButton("Ok") { dialog, which ->
            dialog.dismiss()
            listener.onClick(dialog, which)
        }
        builder.create().show()
    }


    override fun onClick(v: View) {
        super.onClick(v)
        when (v) {
            binding?.cbAddService, binding?.serviceListingEmpty?.cbAddService -> {
                startFragmentActivity(
                    FragmentType.SERVICE_DETAIL_VIEW,
                    bundle = sendBundleData(null),
                    isResult = true
                )
            }
        }
    }

    private fun openSortingBottomSheet() {
        val sortSheet = SortAndFilterBottomSheet()
        sortSheet.onClicked = { }
        sortSheet.show(
            this@ServiceListingFragment.parentFragmentManager,
            ImagePickerBottomSheet::class.java.name
        )
    }

    private fun sandbarNoInternet(context: Activity) {
        val snackBar = Snackbar.make(
            context.findViewById(android.R.id.content),
            context.getString(R.string.noInternet),
            Snackbar.LENGTH_LONG
        )
        snackBar.view.setBackgroundColor(
            ContextCompat.getColor(
                context,
                R.color.snackbar_negative_color
            )
        )
        snackBar.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            val isRefresh = data?.getBooleanExtra(IntentConstant.IS_UPDATED.name, false) ?: false
            if (isRefresh) {
                this.offSet = PAGE_START
                this.limit = PAGE_SIZE + 1
                getListServiceFilterApi(isFirst = true, offSet = offSet, limit = limit)
            }
        }
    }

    override fun showProgress(title: String?, cancelable: Boolean?) {
        binding?.progress?.visible()
    }

    override fun hideProgress() {
        binding?.progress?.gone()
    }
}


