package com.odoo.core.support;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public abstract class OdooActivity extends AppCompatActivity {

    private OnOdooActivityResultListener mOnOdooActivityResultListener;

    public View getContentView() {
        return findViewById(android.R.id.content);
    }

    protected void setText(int res_id, String value) {
        CBind.setText(findViewById(res_id), value);
    }

    public Bundle getArgs() {
        return getIntent().getExtras();
    }


    public void setOnActivityResultListener(OnOdooActivityResultListener callback) {
        mOnOdooActivityResultListener = callback;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mOnOdooActivityResultListener != null) {
            mOnOdooActivityResultListener.onOdooActivityResult(requestCode, resultCode, data);
        }
    }

    public interface OnOdooActivityResultListener {
        void onOdooActivityResult(int requestCode, int resultCode, Intent data);
    }
}
