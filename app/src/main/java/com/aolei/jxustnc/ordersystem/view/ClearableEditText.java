package com.aolei.jxustnc.ordersystem.view;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.aolei.jxustnc.ordersystem.R;

/**
 * 自定义EditText实现带清楚功能
 * Created by NewOr on 2016/4/28.
 */
public class ClearableEditText extends AppCompatEditText implements View.OnTouchListener, View.OnFocusChangeListener, TextWatcher {

    private Drawable mClearTextIcon;
    private OnFocusChangeListener mOnFocusChangeListener;
    private OnTouchListener mOnTouchListener;

    public void setmOnFocusChangeListener(OnFocusChangeListener mOnFocusChangeListener) {
        this.mOnFocusChangeListener = mOnFocusChangeListener;
    }

    public void setmOnTouchListener(OnTouchListener mOnTouchListener) {
        this.mOnTouchListener = mOnTouchListener;
    }

    public ClearableEditText(Context context) {
        super(context);
        initView(context);
    }

    public ClearableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ClearableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    /**
     * 初始化
     * 创建drawable，并为其加入Touch、Focus事件处理
     * 加入TextChangedListener，监听EditText内容变化
     *
     * @param context
     */
    private void initView(Context context) {
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_cancel);
        Drawable wrapperDrawable = DrawableCompat.wrap(drawable);//Wrap the drawable so that it can be tinted pre Lollipop
        DrawableCompat.setTint(wrapperDrawable, getCurrentHintTextColor());
        mClearTextIcon = wrapperDrawable;
        mClearTextIcon.setBounds(0, 0, 60, 60);//设置图片的大小
        super.setOnTouchListener(this);
        super.setOnFocusChangeListener(this);
        addTextChangedListener(this);
    }

    /**
     * 实现Listener
     * 首先检查了清除按钮是否为显示状态，然后判断点击的范围是否在清除按钮内，
     * 如果在范围内的话，在ACTION_UP时清空输入框内容，否则执行mOnTouchListener的
     * onTouch方法。
     *
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int x = (int) event.getX();
        if (mClearTextIcon.isVisible() && x > getWidth() - getPaddingRight() - mClearTextIcon.getIntrinsicWidth()) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                setText("");
            }
            return true;
        }
        return mOnTouchListener != null && mOnTouchListener.onTouch(v, event);
    }

    /**
     * 实现Listener
     * 在获取焦点时，判断输入框中内容是否大于0，有内容则显示清除按钮。
     *
     * @param v
     * @param hasFocus
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            setClearIconVisible(getText().length() > 0);
        } else {
            setClearIconVisible(false);
        }
        if (mOnFocusChangeListener != null) {
            mOnFocusChangeListener.onFocusChange(v, hasFocus);
        }
    }

    /**
     * 使用setClearIconVisible(false)隐藏了清除按钮，在输入文本时才会显示
     *
     * @param visible
     */
    private void setClearIconVisible(final boolean visible) {
        mClearTextIcon.setVisible(visible, false);
        Drawable[] compoundDrawables = getCompoundDrawables();
        setCompoundDrawables(compoundDrawables[0], compoundDrawables[1], visible ? mClearTextIcon : null, compoundDrawables[3]);

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    /**
     * 判断输入框中的字数，大于0则显示清除按钮，否则隐藏
     *
     * @param text
     * @param start
     * @param lengthBefore
     * @param lengthAfter
     */
    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (isFocused()) {
            setClearIconVisible(text.length() > 0);
        }
    }
}
