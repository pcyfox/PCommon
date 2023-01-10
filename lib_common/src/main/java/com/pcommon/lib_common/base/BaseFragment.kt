package com.pcommon.lib_common.base

import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.pcommon.lib_common.application.BaseAbstractApplication.Companion.application


/**
 * @author pcy
 * @describe fragment 基类
 */

@Keep
abstract class BaseFragment<VDB : ViewDataBinding, VM : BaseViewModel>(var vmClass: Class<VM>? = null) :
    Fragment(), OnKeyDownListener {
    var tag: Any? = null

    protected var viewModel: VM? = null
    protected var contentView: View? = null

    private var viewDataBinding: VDB? = null
    protected abstract val fragmentLayoutId: Int
    open var mainViewModelId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = if (vmClass != null) {
            createViewModel(vmClass!!)
        } else {
            createViewModel()
        }

        if (viewModel != null) {
            lifecycle.addObserver(viewModel!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (contentView != null) {
            return contentView
        }

        if (mainViewModelId != -1) {
            viewDataBinding = DataBindingUtil.inflate(inflater, fragmentLayoutId, container, false)
            viewDataBinding?.setVariable(mainViewModelId, viewModel)
            //注入Lifecycle生命周期
            viewDataBinding?.lifecycleOwner = this
            return viewDataBinding?.root
        }
        return inflater.inflate(fragmentLayoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
        initData()
        initView()
        initListener()
    }


    override fun onDestroy() {
        super.onDestroy()
        if (viewModel != null) {
            lifecycle.removeObserver(viewModel!!)
        }
    }


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        // 当fragment用户可见时，才去请求数据，这样实现fragement的懒加载
        if (isVisibleToUser) {
            loadData()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return false;
    }


    fun <T : BaseViewModel> getViewModel(clazz: Class<T>): T {
        return ViewModelProvider(this).get(clazz)

    }

    private fun createViewModel(clazz: Class<VM>): VM {
        return getViewModel(clazz)
    }

    open fun createViewModel(): VM {
        return BaseViewModel(requireActivity().application) as VM
    }

    open fun loadData() {

    }

    open fun initData() {

    }

    open fun observeData() {

    }

    open fun initListener() {

    }

    open fun initView() {}


}
