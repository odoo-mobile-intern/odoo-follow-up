/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p>
 * Created on 25/1/17 11:13 AM
 */
package com.odoo.followup.call;

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
