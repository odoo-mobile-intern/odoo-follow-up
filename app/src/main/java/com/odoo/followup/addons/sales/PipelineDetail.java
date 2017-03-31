package com.odoo.followup.addons.sales;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
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
            findViewById(R.id.label_customer).setVisibility(View.VISIBLE);
            findViewById(R.id.textCustomerName).setVisibility(View.VISIBLE);
            CBind.setText(findViewById(R.id.textCustomerName), customer.getName(row.getInt("partner_id")));
        }
        if (!row.getString("email_from").equals("false")) {
            findViewById(R.id.label_email).setVisibility(View.VISIBLE);
            findViewById(R.id.textCustomerEmail).setVisibility(View.VISIBLE);
            CBind.setText(findViewById(R.id.textCustomerEmail), row.getString("email_from"));
        }
        CBind.setText(findViewById(R.id.textStage), stage.getName(row.getInt("stage_id")));
        if (row.get("next_activity_id") != null && !stage.getName(row.getInt("next_activity_id")).equals("false"))
            CBind.setText(findViewById(R.id.textNextActivity), activity.getName(row.getInt("next_activity_id")) + " on ");

        CBind.setText(findViewById(R.id.textDateAction), row.getString("date_action"));
        if (!row.getString("title_action").equals("false")) {
            findViewById(R.id.textTitleAction).setVisibility(View.VISIBLE);
            CBind.setText(findViewById(R.id.textTitleAction), row.getString("title_action"));
        }
        if (!row.getString("date_deadline").equals("false")) {
            findViewById(R.id.label_deadlinr).setVisibility(View.VISIBLE);
            findViewById(R.id.textDateDeadline).setVisibility(View.VISIBLE);
            CBind.setText(findViewById(R.id.textDateDeadline), row.getString("date_deadline"));
        }
        CBind.setText(findViewById(R.id.textSalesperson), users.getName(row.getInt("user_id")));

        if (row.get("team_id") != null && !team.getName(row.getInt("team_id")).equals("false")) {
            findViewById(R.id.label_salesTeam).setVisibility(View.VISIBLE);
            findViewById(R.id.textSalesTeam).setVisibility(View.VISIBLE);
            CBind.setText(findViewById(R.id.textSalesTeam), team.getName(row.getInt("team_id")));
        }

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
