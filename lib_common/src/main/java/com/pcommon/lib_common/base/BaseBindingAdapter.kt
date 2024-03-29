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
 */
@Keep
abstract class BaseBindingAdapter<M, B : ViewDataBinding>(val context: Context) :
    RecyclerView.Adapter<BaseBindingAdapter.AdapterViewHolder>() {
    var isAutoNotifyChange = true
    var list = arrayListOf<M>()
        @SuppressLint("NotifyDataSetChanged") set(value) {
            field = value
            notifyDataSetChanged()
        }

    @SuppressLint("NotifyDataSetChanged")
    fun addAll(c: Collection<M>, isClear: Boolean = true) {
        if (isClear) list.clear()
        list.addAll(c)
        if (isClear && isAutoNotifyChange) {
            notifyDataSetChanged()
        } else {
            val start = list.size - c.size - 1
            if (isAutoNotifyChange) notifyItemRangeChanged(if (start < 0) 0 else start, c.size)
        }
    }

    fun add(item: M, index: Int = list.size) {
        list.add(if (index < 0) 0 else index, item)
        if (isAutoNotifyChange) notifyItemInserted(index)
    }

    fun remove(item: M): Boolean {
        val index = list.indexOf(item)
        return remove(index)
    }

    fun remove(index: Int): Boolean {
        if (index < 0 || index >= list.size) return false
        list.removeAt(index)
        if (isAutoNotifyChange) notifyItemRemoved(index)
        return true
    }

    fun contains(m: M): Boolean {
        return list.contains(m)
    }


    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        list.clear()
        if (isAutoNotifyChange) notifyDataSetChanged()
    }

    fun isEmpty() = list.isEmpty()

    fun size() = list.size

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
        val item = list[if (position >= list.size) list.size - 1 else position]
        DataBindingUtil.getBinding<B>(holder.itemView)?.run {
            onBindItem(this, item, holder)
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