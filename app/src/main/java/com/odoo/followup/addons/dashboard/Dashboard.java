package com.odoo.followup.addons.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.odoo.followup.utils.support.BaseFragment;
import com.odoo.followup.R;

public class Dashboard extends BaseFragment {

    public Dashboard() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasFABButton(false);
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

}
