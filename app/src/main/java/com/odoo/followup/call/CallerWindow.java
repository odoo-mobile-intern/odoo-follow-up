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
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.odoo.followup.R;
import com.odoo.followup.orm.data.ListRow;

public class CallerWindow {
    public static final String TAG = CallerWindow.class.getSimpleName();
    private ListRow callDetail;
    private Context mContext;
    private WindowManager windowManager;

    public CallerWindow(Context context, ListRow callDetail) {
        this.callDetail = callDetail;
        mContext = context;
        windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Log.e(">>>>>>>>", callDetail.getString("name") + " is calling");
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
        params.gravity = Gravity.TOP;
        return params;
    }

    public View getView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.caller_window_layout, null);
        return view;
    }

    public void bindView() {
        TextView textCallername;
        textCallername = (TextView) getView().findViewById(R.id.textCallerName);
        if (callDetail != null)
            textCallername.setText(callDetail.getString("name"));
    }

    public boolean isLollipop() {
        return (android.os.Build.VERSION.SDK_INT > 19);
    }

    public void showCaller() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                bindView();
                WindowManager.LayoutParams params = getWindowParams();
                params.gravity = Gravity.CENTER;
                windowManager.addView(getView(), params);
            }
        }, 1000);
    }

}
