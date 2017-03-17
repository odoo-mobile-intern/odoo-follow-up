package com.odoo.followup.addons.sales;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.odoo.core.support.CBind;
import com.odoo.followup.R;
import com.odoo.followup.addons.customers.models.ResPartner;
import com.odoo.followup.addons.sales.models.CRMActivity;
import com.odoo.followup.addons.sales.models.CRMLead;
import com.odoo.followup.orm.OListAdapter;
import com.odoo.followup.orm.data.ListRow;
import com.odoo.followup.utils.support.BaseFragment;

public class NextActivity extends BaseFragment implements OListAdapter.OnViewBindListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private OListAdapter adapter;
    private CRMLead opportunities;
    private ResPartner partner;
    private CRMActivity crmActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_next_activity, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        partner = new ResPartner(getContext());
        opportunities = new CRMLead(getContext());
        crmActivity = new CRMActivity(getContext());
        ListView opportunitiesList = (ListView) view.findViewById(R.id.opportunitiesList);
        adapter = new OListAdapter(getContext(), null, R.layout.next_activity_list_item);
        adapter.setOnViewBindListener(this);
        opportunitiesList.setAdapter(adapter);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onViewBind(View view, Cursor cursor, ListRow row) {
        CBind.setText(view.findViewById(R.id.textNextActionName), row.getString("name"));
        CBind.setText(view.findViewById(R.id.textDateAction), "Next activity on " + row.getString("date_action"));
        if (row.get("partner_id") != null)
            CBind.setText(view.findViewById(R.id.textCustomer), partner.getName(row.getInt("partner_id")));
        else view.findViewById(R.id.textCustomer).setVisibility(View.GONE);
        CBind.setText(view.findViewById(R.id.textRevenue), row.getString("planned_revenue"));
        CBind.setText(view.findViewById(R.id.textProbability), "at " + row.getString("probability") + " %");

        if (row.get("next_activity_id") != null) {
            String name = crmActivity.getName(row.getInt("next_activity_id"));
            if (!name.equals("false"))
                CBind.setText(view.findViewById(R.id.textTag), name);
            else view.findViewById(R.id.textTag).setVisibility(View.GONE);
        } else view.findViewById(R.id.textTag).setVisibility(View.GONE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(), opportunities.getUri(), null, "type = ? and date_action != ? ",
                new String[]{"opportunity", "false"}, "date_action DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.changeCursor(data);
        if (opportunities.isEmpty()) {
            Toast.makeText(getContext(), R.string.getting_data, Toast.LENGTH_LONG).show();
            syncUtils(opportunities).sync(null);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.changeCursor(null);
    }
}
