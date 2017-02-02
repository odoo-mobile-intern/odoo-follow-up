package com.odoo.core.support;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

public abstract class OdooActivity extends AppCompatActivity {

    public View getContentView() {
        return findViewById(android.R.id.content);
    }

    protected void setText(int res_id, String value) {
        CBind.setText(findViewById(res_id), value);
    }
}
