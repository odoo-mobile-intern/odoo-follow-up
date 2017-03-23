package com.odoo.followup.addons.dashboard;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.odoo.core.support.CBind;
import com.odoo.followup.R;
import com.odoo.followup.addons.sales.models.CRMTeam;
import com.odoo.followup.orm.OListAdapter;
import com.odoo.followup.orm.data.ListRow;
import com.odoo.followup.utils.support.BaseFragment;

public class Dashboard extends BaseFragment implements OListAdapter.OnViewBindListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private CRMTeam crmTeam;
    private OListAdapter listAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasFABButton(false);
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        crmTeam = new CRMTeam(getContext());
        ListView listView = (ListView) view.findViewById(R.id.dashboardGridView);
        listAdapter = new OListAdapter(getContext(), null, R.layout.dashboard_list_items);
        listAdapter.setOnViewBindListener(this);
        listView.setAdapter(listAdapter);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onViewBind(View view, Cursor cursor, ListRow row) {
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressInvoice);
        progressBar.getProgressDrawable().setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        progressBar.setMax(row.getInt("invoiced_target"));
        progressBar.setProgress(row.getInt("invoiced"));
        CBind.setText(view.findViewById(R.id.textCrmTeamName), row.getString("name"));
        CBind.setText(view.findViewById(R.id.textInvoiceThisMonth), row.getString("invoiced") + " /");
        CBind.setText(view.findViewById(R.id.textTargetInvoice), getTarget(row.getString("invoiced_target")));
    }

    private String getTarget(String invoiced_target) {
        int amount = (Integer.parseInt(invoiced_target)) / 1000;
        if (amount > 0)
            return amount + " k";
        else
            return invoiced_target;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(), crmTeam.getUri(), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        listAdapter.changeCursor(data);
        if (crmTeam.isEmpty()) {
            Toast.makeText(getContext(), R.string.getting_data, Toast.LENGTH_LONG).show();
            syncUtils(crmTeam).sync(null);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        listAdapter.changeCursor(null);
    }
}
