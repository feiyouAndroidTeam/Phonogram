package com.yc.phonogram.ui.popupwindow;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.IdRes;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.yc.phonogram.ui.IView;
import com.yc.phonogram.utils.Mp3Utils;
import com.yc.phonogram.utils.NavgationBarUtils;


/**
 * Created by zhangkai on 2017/10/24.
 */

public abstract class BasePopupWindow extends PopupWindow implements IView {
    protected Activity mContext;
    protected View mRootView;


    public Activity getContext() {
        return mContext;
    }

    private ColorDrawable mBackgroundDrawable;

    public BasePopupWindow(Activity context) {
        super(context);
        this.mContext = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        mRootView = inflater.inflate(getLayoutId(), null);

        try {
            mRootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
            );
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mRootView.setSystemUiVisibility(mRootView.getSystemUiVisibility() | View
                        .SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        } catch (Exception e) {
        }

        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);

        setWindowAlpha(0.5f);
        setContentView(mRootView);
        setOutsideTouchable(false);

        init();
    }

    public View getView(@IdRes int id) {
        return mRootView.findViewById(id);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        setWindowAlpha(1.f);
    }

    private void setWindowAlpha(float alpha) {
        Window window = mContext.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();

        lp.alpha = alpha;
        window.setAttributes(lp);
    }

    @Override
    public void setContentView(View contentView) {
        if (contentView != null) {
            super.setContentView(contentView);
            setFocusable(true);
            setTouchable(true);
            contentView.setFocusable(true);
            contentView.setFocusableInTouchMode(true);
            contentView.setOnKeyListener(new View.OnKeyListener() {

                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_BACK:
                            dismiss();
                            return true;
                        default:
                            break;
                    }
                    return false;
                }
            });
        }
    }

    public void show() {
        show(mContext.getWindow().getDecorView().getRootView(), Gravity.CENTER);
    }

    public void show(View view) {
        show(view, Gravity.CENTER);
    }

    public void show(View view, int gravity) {
        showAtLocation(view, gravity, 0, NavgationBarUtils.getNavigationBarHeight(mContext));
    }

    @Override
    public void setOutsideTouchable(boolean touchable) {
        super.setOutsideTouchable(touchable);
        if (touchable) {
            if (mBackgroundDrawable == null) {
                mBackgroundDrawable = new ColorDrawable(0x00000000);
            }
            super.setBackgroundDrawable(mBackgroundDrawable);
        } else {
            super.setBackgroundDrawable(null);
        }
    }
}
