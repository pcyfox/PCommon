package com.pcommon.lib_vidget.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatCheckBox;

import com.pcommon.lib_vidget.R;

public class MyCheckBox extends AppCompatCheckBox {

    private int drawbleW;
    private int drawbleH;

    public MyCheckBox(Context context) {
        this(context, null);
    }

    public MyCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MyCheckBox);
        Drawable drawable = array.getDrawable(R.styleable.MyCheckBox_android_button);
        drawbleW = (int) array.getDimension(R.styleable.MyCheckBox_attr_drawableWidth, 30);
        drawbleH = (int) array.getDimension(R.styleable.MyCheckBox_attr_drawableHight, 30);
        setDrawableSize(drawbleW, drawbleH);
        setCompoundDrawablePadding(4);
        setButtonDrawable(drawable);
        array.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public MyCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setButtonDrawable(int resId) {
        setButtonDrawable(AppCompatResources.getDrawable(getContext(), resId));
    }

    //一定要在设置 Drawable 之前设置 否者不生效
    public void setDrawableSize(int drawbleW, int drawbleH) {
        this.drawbleW = drawbleW;
        this.drawbleH = drawbleH;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Drawable bd = getButtonDrawable();
            if(bd!=null){
                bd.setBounds(0, 0, drawbleW, drawbleH);
            }
        }
        invalidate();
    }

    @Override
    public void setButtonDrawable(Drawable buttonDrawable) {
        if (buttonDrawable != null) {
            buttonDrawable.setBounds(0, 0, drawbleW, drawbleH);
            setCompoundDrawables(buttonDrawable, null, null, null);
        }
    }
}
