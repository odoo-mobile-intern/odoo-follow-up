package com.odoo.followup.addons.sales;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.RatingBar;

import com.odoo.core.support.CBind;
import com.odoo.core.utils.OAppBarUtils;
import com.odoo.followup.R;
import com.odoo.followup.addons.customers.models.ResPartner;
import com.odoo.followup.addons.sales.models.CRMActivity;
import com.odoo.followup.addons.sales.models.CRMLead;
import com.odoo.followup.addons.sales.models.CRMTeam;
import com.odoo.followup.addons.sales.models.CrmStage;
import com.odoo.followup.orm.data.ListRow;
import com.odoo.followup.orm.models.ResUsers;

import java.text.NumberFormat;
import java.util.Locale;

public class PipelineDetail extends AppCompatActivity {

    private CRMLead lead;
    private ResPartner customer;
    private CrmStage stage;
    private CRMActivity activity;
    private ResUsers users;
    private CRMTeam team;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pipeline_detail);
        OAppBarUtils.setAppBar(this, false);
        setTitle("Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        lead = new CRMLead(this);
        customer = new ResPartner(this);
        stage = new CrmStage(this);
        activity = new CRMActivity(this);
        users = new ResUsers(this);
        team = new CRMTeam(this);

        setDetails();
    }

    private void setDetails() {
        int id = getIntent().getIntExtra("_id", -1);
        ListRow row = lead.browse(id);

        CBind.setText(findViewById(R.id.textOppName), row.getString("name"));
        CBind.setText(findViewById(R.id.textPlannedRevenue), getCurrency(row.getFloat("planned_revenue")));
        CBind.setText(findViewById(R.id.textProbability), row.getFloat("probability") + "%");

        if (row.get("partner_id") != null) {
            CBind.setText(findViewById(R.id.textCustomerName), customer.getName(row.getInt("partner_id")));
        }
        CBind.setText(findViewById(R.id.textCustomerEmail), row.getString("email_from"));
        CBind.setText(findViewById(R.id.textNextActivity), activity.getName(row.getInt("next_activity_id")) + " on ");

        CBind.setText(findViewById(R.id.textDateAction), row.getString("date_action"));
        CBind.setText(findViewById(R.id.textTitleAction), row.getString("title_action"));
        CBind.setText(findViewById(R.id.textDateDeadline), row.getString("date_deadline"));
        CBind.setText(findViewById(R.id.textSalesperson), users.getName(row.getInt("user_id")));

        CBind.setText(findViewById(R.id.textSalesTeam), team.getName(row.getInt("team_id")));

        RatingBar oppRating = (RatingBar) findViewById(R.id.oppRating);
        String priority = row.getString("priority");
        switch (priority) {
            case "0":
                oppRating.setRating(Float.parseFloat("0.0"));
                break;
            case "1":
                oppRating.setRating(Float.parseFloat("1.0"));
                break;
            case "2":
                oppRating.setRating(Float.parseFloat("2.0"));
                break;
            case "3":
                oppRating.setRating(Float.parseFloat("3.0"));
                break;
        }
    }

    private String getCurrency(double number) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(number);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
