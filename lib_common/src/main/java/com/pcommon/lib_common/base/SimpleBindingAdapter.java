package com.pcommon.lib_common.base;

import android.content.Context;

import androidx.annotation.LayoutRes;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

public abstract class SimpleBindingAdapter<M, B extends ViewDataBinding> extends BaseBindingAdapter<M, B> {

    private int layout;

    public SimpleBindingAdapter(Context context, int layout) {
        super(context);
        this.layout = layout;
    }

    protected @LayoutRes
    int getLayoutResId(int viewType) {
        return this.layout;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    protected abstract void onAdapterBindItem(B binding, M item, RecyclerView.ViewHolder holder);

    @Override
    protected void onBindItem(ViewDataBinding binding, Object item, RecyclerView.ViewHolder holder) {
        onAdapterBindItem((B) binding, (M) item, holder);
    }


}
