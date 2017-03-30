package com.odoo.followup.addons.dashboard;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.odoo.core.support.CBind;
import com.odoo.followup.R;
import com.odoo.followup.addons.dashboard.models.UserPerformance;
import com.odoo.followup.addons.sales.Pipeline;
import com.odoo.followup.addons.sales.models.CRMTeam;
import com.odoo.followup.orm.data.ListRow;
import com.odoo.followup.orm.sync.OSyncUtils;
import com.odoo.followup.utils.support.BaseFragment;
import com.odoo.widget.recycler.CursorRowBuilder;
import com.odoo.widget.recycler.EasyRecyclerView;
import com.odoo.widget.recycler.EasyRecyclerViewAdapter;

import java.text.NumberFormat;
import java.util.Locale;

public class Dashboard extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>,
        EasyRecyclerViewAdapter.OnViewBindListener {

    private CRMTeam crmTeam;
    private EasyRecyclerView recyclerView;
    private UserPerformance userPerformance;

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
        userPerformance = new UserPerformance(getContext());

        recyclerView = (EasyRecyclerView) findViewById(R.id.dashboardRecyclerView);
        recyclerView.setLayout(R.layout.dashboard_list_items);
        GridLayoutManager gridLayoutManager = recyclerView.grid(2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position == 0 ? 2 : 1;
            }
        });
        recyclerView.setOnViewBindListener(this);
        getLoaderManager().initLoader(0, null, this);
    }

    private String getAmount(String amount) {
        int amt = (Integer.parseInt(amount)) / 1000;
        if (amt > 0)
            return amt + " k";
        else
            return amount;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(), crmTeam.getUri(), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        CursorRowBuilder builder = new CursorRowBuilder(new String[]{"is_dashboard"});
        builder.addRow(new String[]{"true"});
        recyclerView.changeCursor(builder.getWithCursors(data));
        if (crmTeam.isEmpty()) {
            Toast.makeText(getContext(), R.string.getting_data, Toast.LENGTH_LONG).show();
            syncUtils(crmTeam).sync(null);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        recyclerView.changeCursor(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        parent().setTitle(R.string.title_dashboard);
        userPerformance.syncPerformance();
        OSyncUtils.get(getContext(), crmTeam).sync(new Bundle());
    }

    @Override
    public void onViewBind(int position, View view, Cursor cursor) {
        boolean isDashboard = cursor.getColumnIndex("is_dashboard") != -1;
        view.findViewById(R.id.userPerformanceContainer).setVisibility(!isDashboard ? View.GONE : View.VISIBLE);
        view.findViewById(R.id.itemView).setVisibility(isDashboard ?
                View.GONE : View.VISIBLE);

        if (!isDashboard) {
            ListRow row = new ListRow(cursor);
            ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressInvoice);
            progressBar.setMax(row.getInt("invoiced_target"));
            progressBar.setProgress(row.getInt("invoiced"));
            CBind.setText(view.findViewById(R.id.textCrmTeamName), row.getString("name"));
            CBind.setText(view.findViewById(R.id.textInvoiceThisMonth), getAmount(row.getString("invoiced")) + " /");
            CBind.setText(view.findViewById(R.id.textTargetInvoice), getAmount(row.getString("invoiced_target")));
        } else {
            bindUserPerformance(view);
        }
    }

    private void bindUserPerformance(View view) {
        ListRow row = userPerformance.getUserPerformance();
        view.findViewById(R.id.showPipeline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parent().startFragment(new Pipeline(), "Pipeline", true);
            }
        });
        if (row != null) {
            // To-do
            if (row.getRow("meeting") != null && row.getRow("activity") != null) {
                CBind.setText(view.findViewById(R.id.todayMeetings), row.getRow("meeting").getString("today"));
                CBind.setText(view.findViewById(R.id.todayActivity), row.getRow("activity").getString("today"));
                CBind.setText(view.findViewById(R.id.next7DayMeeting), row.getRow("meeting").getString("next_7_days"));
                CBind.setText(view.findViewById(R.id.next7DayActivity), row.getRow("activity").getString("next_7_days"));
            }
            if (row.getRow("done") != null) {
                // Performance
                // done
                CBind.setText(view.findViewById(R.id.doneActivityThisMonth), row.getRow("done").getString("this_month"));
                CBind.setText(view.findViewById(R.id.doneActivityTarget), row.getRow("done").getString("target")); // TODO: Click to set
                CBind.setText(view.findViewById(R.id.doneActivityLastMonth), row.getRow("done").getString("last_month"));
            }
            if (row.getRow("won") != null && row.getRow("target") != null) {
                // won
                CBind.setText(view.findViewById(R.id.wonOppThisMonth), getCurrency(row.getRow("won").getDouble("this_month")));
                CBind.setText(view.findViewById(R.id.wonOppTarget), row.getRow("won").getString("target")); // TODO: Click to set
                CBind.setText(view.findViewById(R.id.wonOppLastMonth), getCurrency(row.getRow("won").getDouble("last_month")));
            }
            if (row.getRow("invoiced") != null && row.getRow("target") != null) {
                // invoice
                CBind.setText(view.findViewById(R.id.invoiceThisMonth), getCurrency(row.getRow("invoiced").getDouble("this_month")));
                CBind.setText(view.findViewById(R.id.invoiceTarget), row.getRow("invoiced").getString("target")); // TODO: Click to set
                CBind.setText(view.findViewById(R.id.invoiceLastMonth), getCurrency(row.getRow("invoiced").getDouble("last_month")));
            }
        }
    }


    private String getCurrency(double number) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(number);
    }
}
