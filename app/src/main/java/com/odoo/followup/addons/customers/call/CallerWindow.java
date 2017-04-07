package com.odoo.followup.addons.customers.call;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.odoo.followup.R;
import com.odoo.followup.orm.data.ListRow;
import com.odoo.followup.utils.BitmapUtils;

public class CallerWindow {
    public static final String TAG = CallerWindow.class.getSimpleName();
    private static CallerWindow callerWindow;
    private ListRow callDetail;
    private Context mContext;
    private String number;
    private WindowManager windowManager;
    private View callerView = null;

    public static void show(Context context, String number, ListRow detail) {
        callerWindow = new CallerWindow(context, number, detail);
        callerWindow.showCaller();
    }

    public static void remove() {
        if (callerWindow != null)
            callerWindow.removeWindow();
    }

    private CallerWindow(Context context, String number, ListRow callDetail) {
        this.callDetail = callDetail;
        this.number = number;
        mContext = context;
        windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
    }

    private WindowManager.LayoutParams getWindowParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        return params;
    }

    private View getView() {
        return LayoutInflater.from(mContext).inflate(R.layout.caller_window_layout, null);
    }

    private void bindView() {
        TextView txvCallerName, txvCallNumber;
        txvCallerName = (TextView) callerView.findViewById(R.id.textCallerName);
        txvCallNumber = (TextView) callerView.findViewById(R.id.txtCallerNumber);
        txvCallerName.setText(callDetail.getString("name"));
        txvCallNumber.setText(number);
        if (!callDetail.getString("image_medium").equals("false")) {
            ImageView avatar = (ImageView) callerView.findViewById(R.id.callerImage);
            avatar.setImageBitmap(BitmapUtils.getBitmapImage(mContext, callDetail.getString("image_medium")));
        }
        callerView.findViewById(R.id.closePopupWindow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeWindow();
            }
        });
    }

    private void removeWindow() {
        if (callerView != null) {
            windowManager.removeViewImmediate(callerView);
            callerView = null;
        }
    }

    private void showCaller() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                WindowManager.LayoutParams params = getWindowParams();
                callerView = getView();
                bindView();
                windowManager.addView(callerView, params);
            }
        }, 1000);
    }

}
