package com.odoo.followup;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

public class BaseFragment extends Fragment {
    public static final String TAG = BaseFragment.class.getSimpleName();

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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

}
