package com.odoo.followup.utils.support;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.odoo.followup.HomeActivity;
import com.odoo.followup.R;
import com.odoo.followup.orm.OModel;
import com.odoo.followup.orm.sync.OSyncUtils;

public class BaseFragment extends Fragment {
    public static final String TAG = BaseFragment.class.getSimpleName();
    private View contentView;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        contentView = view;
        setHasFABButton(false);
        setOnFABClick(null);
    }

    public void setHasFABButton(boolean hasFABButton) {
        parent().setFabVisible(hasFABButton);
    }

    public void setOnFABClick(View.OnClickListener click) {
        parent().findViewById(R.id.fab).setOnClickListener(click);
    }

    public HomeActivity parent() {
        return (HomeActivity) getActivity();
    }

    public OSyncUtils syncUtils(OModel model) {
        return OSyncUtils.get(getContext(), model);
    }


    public View findViewById(int res_id) {
        return contentView.findViewById(res_id);
    }
}
