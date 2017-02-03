package com.odoo.followup.addons.sales;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.odoo.followup.utils.support.BaseFragment;
import com.odoo.followup.R;

public class NextActivity extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_next_activity, container, false);
    }

}
