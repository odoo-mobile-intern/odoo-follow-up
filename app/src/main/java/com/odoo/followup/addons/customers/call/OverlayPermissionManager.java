package com.odoo.followup.addons.customers.call;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.odoo.core.support.OdooActivity;

public class OverlayPermissionManager implements OdooActivity.OnOdooActivityResultListener {
    public final static int REQUEST_CODE = 150;
    private OdooActivity mActivity;
    private OnOverlayPermissionListener mOnOverlayPermissionListener;

    public OverlayPermissionManager(OdooActivity activity) {
        mActivity = activity;
        mActivity.setOnActivityResultListener(this);
    }

    @SuppressLint("NewApi")
    public void checkDrawOverlayPermission(OnOverlayPermissionListener callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mOnOverlayPermissionListener = callback;
            if (!Settings.canDrawOverlays(mActivity)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + mActivity.getPackageName()));
                mActivity.startActivityForResult(intent, REQUEST_CODE);
            }
        } else if (callback != null) {
            callback.canDrawerOverlays(true);
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onOdooActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (mOnOverlayPermissionListener != null) {
                mOnOverlayPermissionListener.canDrawerOverlays(Settings.canDrawOverlays(mActivity));
            }
        }
    }

    public interface OnOverlayPermissionListener {
        void canDrawerOverlays(boolean canDraw);
    }
}
