package com.catlogservice.ui

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.catlogservice.R
import com.catlogservice.base.AppBaseActivity
import com.catlogservice.constant.FragmentType
import com.catlogservice.ui.information.ServiceInformationFragment
import com.catlogservice.ui.service.ServiceDetailFragment
import com.framework.base.BaseFragment
import com.framework.base.FRAGMENT_TYPE
import com.framework.databinding.ActivityFragmentContainerBinding
import com.framework.exceptions.IllegalFragmentTypeException
import com.framework.models.BaseViewModel
import com.framework.views.customViews.CustomToolbar


open class FragmentContainerServiceActivity : AppBaseActivity<ActivityFragmentContainerBinding, BaseViewModel>() {

    private var type: FragmentType? = null
    private var serviceDetailFragment: ServiceDetailFragment? = null
    private var serviceInformationFragment: ServiceInformationFragment? = null


    override fun getLayout(): Int {
        return com.framework.R.layout.activity_fragment_container
    }

    override fun getViewModelClass(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        intent?.extras?.getInt(FRAGMENT_TYPE)?.let { type = FragmentType.values()[it] }
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView() {
        super.onCreateView()
        setFragment()
    }

    override fun getToolbar(): CustomToolbar? {
        return binding?.appBarLayout?.toolbar
    }


    override fun getToolbarBackgroundColor(): Int? {
        return when (type) {
            FragmentType.SERVICE_INFORMATION, FragmentType.SERVICE_DETAIL_VIEW -> ContextCompat.getColor(this, R.color.color_primary)
            else -> super.getToolbarBackgroundColor()
        }
    }

    override fun getToolbarTitleColor(): Int? {
        return when (type) {
            FragmentType.SERVICE_INFORMATION, FragmentType.SERVICE_DETAIL_VIEW -> ContextCompat.getColor(this, R.color.white)
            else -> super.getToolbarTitleColor()
        }
    }

    override fun getNavigationIcon(): Drawable? {
        return when (type) {
            FragmentType.SERVICE_INFORMATION, FragmentType.SERVICE_DETAIL_VIEW -> ContextCompat.getDrawable(this, R.drawable.ic_arrow_left)
            else -> super.getNavigationIcon()
        }
    }

    override fun getToolbarTitle(): String? {
        return when (type) {
            FragmentType.SERVICE_INFORMATION -> "Other Information"
            FragmentType.SERVICE_DETAIL_VIEW -> "Service Details"
            else -> super.getToolbarTitle()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val toolbarMenu = menu ?: return super.onCreateOptionsMenu(menu)
        val menuRes = getMenuRes() ?: return super.onCreateOptionsMenu(menu)
        menuInflater.inflate(menuRes, toolbarMenu)
        return true
    }

    open fun getMenuRes(): Int? {
        return when (type) {
            else -> null
        }
    }

    private fun shouldAddToBackStack(): Boolean {
        return when (type) {
            else -> false
        }
    }

    private fun setFragment() {
        val fragment = getFragmentInstance(type)
        fragment?.arguments = intent.extras
        binding?.container?.id?.let { addFragmentReplace(it, fragment, shouldAddToBackStack()) }
    }

    private fun getFragmentInstance(type: FragmentType?): BaseFragment<*, *>? {
        return when (type) {
            FragmentType.SERVICE_DETAIL_VIEW -> {
                serviceDetailFragment = ServiceDetailFragment.newInstance()
                serviceDetailFragment
            }
            FragmentType.SERVICE_INFORMATION -> {
                serviceInformationFragment = ServiceInformationFragment.newInstance()
                serviceInformationFragment
            }
            else -> throw IllegalFragmentTypeException()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}

fun Fragment.startFragmentActivity(type: FragmentType, bundle: Bundle = Bundle(), clearTop: Boolean = false, isResult: Boolean = false) {
    val intent = Intent(activity, FragmentContainerServiceActivity::class.java)
    intent.putExtras(bundle)
    intent.setFragmentType(type)
    if (clearTop) intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    if (isResult.not()) startActivity(intent) else startActivityForResult(intent, 101)
}

fun startFragmentActivityNew(activity: Activity, type: FragmentType, bundle: Bundle = Bundle(), clearTop: Boolean) {
    val intent = Intent(activity, FragmentContainerServiceActivity::class.java)
    intent.putExtras(bundle)
    intent.setFragmentType(type)
    if (clearTop) intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    activity.startActivity(intent)
}

fun AppCompatActivity.startFragmentActivity(type: FragmentType, bundle: Bundle = Bundle(), clearTop: Boolean = false) {
    val intent = Intent(this, FragmentContainerServiceActivity::class.java)
    intent.putExtras(bundle)
    intent.setFragmentType(type)
    if (clearTop) intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
}

fun Intent.setFragmentType(type: FragmentType): Intent {
    return this.putExtra(FRAGMENT_TYPE, type.ordinal)
}
