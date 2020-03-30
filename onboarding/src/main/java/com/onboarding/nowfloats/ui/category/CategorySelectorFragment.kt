package com.onboarding.nowfloats.ui.category

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.framework.base.BaseResponse
import com.framework.extensions.observeOnce
import com.framework.extensions.visible
import com.onboarding.nowfloats.R
import com.onboarding.nowfloats.base.AppBaseFragment
import com.onboarding.nowfloats.constant.IntentConstant
import com.onboarding.nowfloats.constant.RecyclerViewActionType
import com.onboarding.nowfloats.constant.RecyclerViewItemType
import com.onboarding.nowfloats.databinding.FragmentCategorySelectorBinding
import com.onboarding.nowfloats.extensions.getParcelable
import com.onboarding.nowfloats.model.RequestFloatsModel
import com.onboarding.nowfloats.model.category.CategoryDataModel
import com.onboarding.nowfloats.recyclerView.AppBaseRecyclerViewAdapter
import com.onboarding.nowfloats.recyclerView.BaseRecyclerViewItem
import com.onboarding.nowfloats.recyclerView.RecyclerItemClickListener
import com.onboarding.nowfloats.rest.response.category.ResponseDataCategory
import com.onboarding.nowfloats.ui.channel.ChannelPickerActivity
import com.onboarding.nowfloats.viewmodel.category.CategoryViewModel

class CategorySelectorFragment : AppBaseFragment<FragmentCategorySelectorBinding, CategoryViewModel>(), RecyclerItemClickListener {

    private var requestFloatsModel: RequestFloatsModel? = RequestFloatsModel()
    private var baseAdapter: AppBaseRecyclerViewAdapter<CategoryDataModel>? = null
    private var categoryList = ArrayList<CategoryDataModel>()
    private var category: CategoryDataModel? = null

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle? = null): CategorySelectorFragment {
            val fragment = CategorySelectorFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_category_selector
    }

    override fun getViewModelClass(): Class<CategoryViewModel> {
        return CategoryViewModel::class.java
    }

    override fun onCreateView() {
        viewModel?.getCategories(baseActivity)?.observeOnce(viewLifecycleOwner, Observer { onGetCategories(it) })
        setOnClickListener(binding?.next)
    }

    private fun onGetCategories(response: BaseResponse) {
        if (response.error != null) {
            showLongToast(response.error?.localizedMessage); return
        }
        val apiResponse = response as? ResponseDataCategory ?: return
        categoryList.clear()
        apiResponse.data?.let { categoryList.addAll(it) }
        setAdapter(categoryList)
    }

    private fun setAdapter(list: ArrayList<CategoryDataModel>) {
        baseAdapter = AppBaseRecyclerViewAdapter(baseActivity, list, this)
        val gridLayoutManager = GridLayoutManager(baseActivity, 2)
        gridLayoutManager.spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (baseAdapter?.getItemViewType(position)) {
                    RecyclerViewItemType.SECTION_HEADER_ITEM.getLayout() -> 2
                    else -> 1
                }
            }
        }
        binding?.recyclerView?.layoutManager = gridLayoutManager
        binding?.recyclerView?.adapter = baseAdapter
        baseAdapter?.runLayoutAnimation(binding?.recyclerView, R.anim.grid_layout_animation_from_bottom)
    }

    override fun onItemClick(position: Int, item: BaseRecyclerViewItem?, actionType: Int) {
        when (actionType) {
            RecyclerViewActionType.CATEGORY_ITEM_CLICKED.ordinal -> {
                category = item as? CategoryDataModel
                for (listItem in categoryList) {
                    (listItem as? CategoryDataModel)?.let {
                        it.isSelected = (it == item)
                    }
                }
                binding?.next?.visible()
                baseAdapter?.notifyDataSetChanged()
            }
        }

    }

    override fun onClick(v: View) {
        super.onClick(v)
        when (v) {
            binding?.next -> gotoChannelPicker()
        }
    }

    private fun gotoChannelPicker() {
        val bundle = Bundle()
        category?.let { requestFloatsModel?.categoryDataModel = it }
        bundle.putParcelable(IntentConstant.REQUEST_FLOATS_INTENT.name, requestFloatsModel)
        navigator?.startActivity(ChannelPickerActivity::class.java, bundle)
    }
}
