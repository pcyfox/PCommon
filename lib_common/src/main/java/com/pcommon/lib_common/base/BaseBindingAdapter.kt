/* * Copyright 2018-2019 KunMinX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.*/
package com.pcommon.lib_common.base

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

/**
 * @author KunMinX
 * Create at 2018/6/30
 */
@Keep
abstract class BaseBindingAdapter<M, B : ViewDataBinding>(val context: Context) :
    RecyclerView.Adapter<BaseBindingAdapter.AdapterViewHolder>() {

    var list = arrayListOf<M>()
        @SuppressLint("NotifyDataSetChanged") set(value) {
            field = value
            notifyDataSetChanged()
        }

    @SuppressLint("NotifyDataSetChanged")
    fun addAll(c: Collection<M>, isClear: Boolean = true) {
        if (c.isEmpty()) {
            return
        }
        if (isClear) list.clear()
        list.addAll(c)
        if (isClear) {
            notifyDataSetChanged()
        } else {
            val start = list.size - c.size - 1
            notifyItemRangeChanged(if (start < 0) 0 else start, c.size)
        }
    }

    fun add(item: M, index: Int = list.size - 1) {
        list.add(item)
        notifyItemInserted(index)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    fun isEmpty() = list.isEmpty()

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterViewHolder {
        val binding = DataBindingUtil.inflate<B>(
            LayoutInflater.from(context), getLayoutResId(viewType), parent, false
        )
        return AdapterViewHolder(binding as ViewDataBinding)
    }


    override fun onBindViewHolder(holder: AdapterViewHolder, position: Int) {
        DataBindingUtil.getBinding<B>(holder.itemView)?.run {
            onBindItem(this, list[position], holder)
        }
    }

    @LayoutRes
    protected abstract fun getLayoutResId(viewType: Int): Int

    /**
     * 注意：
     * RecyclerView 中的数据有位置改变（比如删除）时一般不会重新调用 onBindViewHolder() 方法，除非这个元素不可用。
     * 为了实时获取元素的位置，我们通过 ViewHolder.getAdapterPosition() 方法。
     *
     * @param binding
     * @param item
     * @param holder
     */
    protected abstract fun onBindItem(binding: B, item: M, holder: ViewHolder)

    class AdapterViewHolder(val binding: ViewDataBinding) : ViewHolder(binding.root) {

    }
}