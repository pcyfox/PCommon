package com.pcommon.lib_common.base

import android.content.Context
import androidx.annotation.Keep
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import com.pcommon.lib_common.base.BaseBindingAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder

@Keep
abstract class SimpleBindingAdapter<M, B : ViewDataBinding>(
    context: Context,
    private val layout: Int
) : BaseBindingAdapter<M, B>(context) {

    @LayoutRes
    override fun getLayoutResId(viewType: Int): Int = layout

    protected abstract fun onAdapterBindItem(binding: B, item: M, holder: ViewHolder)

    override fun onBindItem(binding: B, item: M, holder: ViewHolder) {
        onAdapterBindItem(binding, item, holder)
    }
}