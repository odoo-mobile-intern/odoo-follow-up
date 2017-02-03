package com.odoo.followup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
