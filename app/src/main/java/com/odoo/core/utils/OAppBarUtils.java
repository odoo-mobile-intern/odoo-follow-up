package com.odoo.core.utils;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.odoo.followup.R;


public class OAppBarUtils {

    public static void setAppBar(AppCompatActivity activity, Boolean withHomeButtonEnabled) {
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        if (toolbar != null) {
            activity.setSupportActionBar(toolbar);
            ActionBar actionBar = activity.getSupportActionBar();
            if (withHomeButtonEnabled && actionBar != null) {
                actionBar.setHomeButtonEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }
}