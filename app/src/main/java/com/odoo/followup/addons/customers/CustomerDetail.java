package com.odoo.followup.addons.customers;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.odoo.core.support.CBind;
import com.odoo.followup.R;
import com.odoo.followup.addons.customers.models.ResPartner;
import com.odoo.followup.orm.data.ListRow;
import com.odoo.followup.orm.sync.OSyncUtils;
import com.odoo.followup.utils.BitmapUtils;
import com.odoo.widget.chatter.ChatterView;

import java.util.List;

public class CustomerDetail extends AppCompatActivity implements View.OnClickListener {

    private ResPartner partner;
    private FloatingActionButton fab;
    private ImageView customerImage;
    private int customer_id;
    private CollapsingToolbarLayout collapseToolbar;
    private EditText editName;
    private String strCustomerImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        collapseToolbar = (CollapsingToolbarLayout) findViewById(R.id.profile_collapsing);

        editName = (EditText) findViewById(R.id.editCustomerName);
        partner = new ResPartner(this);
        customer_id = getIntent().getIntExtra("_id", -1);
        fab = (FloatingActionButton) findViewById(R.id.fabEdit);
        fab.setOnClickListener(this);

        customerImage = (ImageView) findViewById(R.id.customer_avatar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setCustomerDetails();
    }

    private void setCustomerDetails() {
        customerImage.setClickable(false);
        ListRow row = partner.browse(customer_id);
        collapseToolbar.setTitle(row.getString("name"));
        strCustomerImage = row.getString("image_medium");

        CBind.setText(findViewById(R.id.textMobileNumber), row.getString("mobile"));
        CBind.setText(findViewById(R.id.textPhoneNumber), row.getString("phone"));
        CBind.setText(findViewById(R.id.textEmail), row.getString("email"));
        CBind.setText(findViewById(R.id.textStreet), row.getString("street"));
        CBind.setText(findViewById(R.id.textStreet2), row.getString("street2"));
        CBind.setText(findViewById(R.id.textCity), row.getString("city"));
        CBind.setText(findViewById(R.id.textPincode), row.getString("zip"));
        CBind.setText(findViewById(R.id.textWebsite), row.getString("website"));
        CBind.setText(findViewById(R.id.textFax), row.getString("fax"));

        //Contact Number
        if (row.getString("mobile").equals("false") && row.getString("phone").equals("false"))
            findViewById(R.id.contactNumberLayout).setVisibility(View.GONE);

        if (row.getString("mobile").equals("false") && !row.getString("phone").equals("false")) {
            findViewById(R.id.mobileLayout).setVisibility(View.GONE);
            findViewById(R.id.imageCall2).setVisibility(View.VISIBLE);
        }

        findViewById(R.id.mobileLayout).setVisibility(
                row.getString("mobile").equals("false") ? View.GONE : View.VISIBLE
        );
        findViewById(R.id.phoneLayout).setVisibility(
                row.getString("phone").equals("false") ? View.GONE : View.VISIBLE
        );

        //Email
        findViewById(R.id.emailLayout).setVisibility(
                row.getString("email").equals("false") ? View.GONE : View.VISIBLE
        );

        //Address
        findViewById(R.id.textStreet).setVisibility(
                row.getString("street").equals("false") ? View.GONE : View.VISIBLE
        );
        findViewById(R.id.textStreet2).setVisibility(
                row.getString("street2").equals("false") ? View.GONE : View.VISIBLE
        );
        findViewById(R.id.textCity).setVisibility(
                row.getString("city").equals("false") ? View.GONE : View.VISIBLE
        );
        findViewById(R.id.textPincode).setVisibility(
                row.getString("zip").equals("false") ? View.GONE : View.VISIBLE
        );

        //Website
        findViewById(R.id.websiteLayout).setVisibility(
                row.getString("website").equals("false") ? View.GONE : View.VISIBLE
        );

        //Fax
        findViewById(R.id.faxLayout).setVisibility(
                row.getString("fax").equals("false") ? View.GONE : View.VISIBLE
        );

        //Avatar
        if (!row.getString("image_medium").equals("false"))
            customerImage.setImageBitmap(BitmapUtils.getBitmapImage(this, row.getString("image_medium")));
        else
            customerImage.setImageBitmap(BitmapUtils.getAlphabetImage(this, row.getString("name")));

        ChatterView chatterView = (ChatterView) findViewById(R.id.chatterView);
        chatterView.loadChatter(partner, row.getInt("id"));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fabEdit:
                findViewById(R.id.viewLayout).setVisibility(View.GONE);
                findViewById(R.id.editLayout).setVisibility(View.VISIBLE);
                setEditCustomerDetail();
                fab.setImageResource(R.drawable.ic_action_done);

                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        saveCustomer();
                    }
                });
                break;
            case R.id.customer_avatar:
                selectImage();
                break;
        }
    }

    private void setEditCustomerDetail() {
        customerImage.setClickable(true);

        List<ListRow> rows = partner.select("id = ?", new String[]{String.valueOf(getIntent().getIntExtra("id", -1))});
        for (ListRow row : rows) {
            editName.setText(row.getString("name"));

            //Contact Number
            CBind.setText(findViewById(R.id.editMobileNumber), row.getString("mobile").equals("false")
                    ? "" : row.getString("mobile"));
            CBind.setText(findViewById(R.id.editPhoneNumber), row.getString("phone").equals("false")
                    ? "" : row.getString("phone"));

            //Email
            CBind.setText(findViewById(R.id.editEmail), row.getString("email").equals("false")
                    ? "" : row.getString("email"));

            //Address
            CBind.setText(findViewById(R.id.editStreet), row.getString("street").equals("false")
                    ? "" : row.getString("street"));
            CBind.setText(findViewById(R.id.editStreet2), row.getString("street2").equals("false")
                    ? "" : row.getString("street2"));
            CBind.setText(findViewById(R.id.editCity), row.getString("city").equals("false")
                    ? "" : row.getString("city"));
            CBind.setText(findViewById(R.id.editPincode), row.getString("zip").equals("false")
                    ? "" : row.getString("zip"));

            //Website
            CBind.setText(findViewById(R.id.editWebsite), row.getString("website").equals("false")
                    ? "" : row.getString("website"));

            //Fax
            CBind.setText(findViewById(R.id.editFax), row.getString("fax").equals("false")
                    ? "" : row.getString("fax"));

            //Avatar
            if (!row.getString("image_medium").equals("false"))
                customerImage.setImageBitmap(BitmapUtils.getBitmapImage(this, row.getString("image_medium")));
            else
                customerImage.setImageBitmap(BitmapUtils.getAlphabetImage(this, row.getString("name")));

            customerImage.setOnClickListener(this);
        }
    }

    private void selectImage() {
        Intent intent = new Intent();

        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 0);
        intent.putExtra("aspectY", 0);
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 200);

        try {
            intent.putExtra("return-data", true);
            startActivityForResult(Intent.createChooser(intent,
                    "Complete action using"), 1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (data != null) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap bitmap = extras.getParcelable("data");
                    customerImage.setImageBitmap(bitmap);
                    strCustomerImage = BitmapUtils.bitmapToBase64(bitmap);
                }
            } else {
                NavUtils.getParentActivityIntent(this);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.editLayout).getVisibility() == View.VISIBLE) {
            findViewById(R.id.editLayout).setVisibility(View.GONE);
            findViewById(R.id.viewLayout).setVisibility(View.VISIBLE);
        } else
            finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveCustomer() {
        EditText editMobile, editPhone, editEmail, editStreet, editStreet2, editZip,
                editWebsite, editFax, editCity;

        editMobile = (EditText) findViewById(R.id.editMobileNumber);
        editPhone = (EditText) findViewById(R.id.editPhoneNumber);
        editEmail = (EditText) findViewById(R.id.editEmail);
        editStreet = (EditText) findViewById(R.id.editStreet);
        editStreet2 = (EditText) findViewById(R.id.editStreet2);
        editWebsite = (EditText) findViewById(R.id.editWebsite);
        editFax = (EditText) findViewById(R.id.editFax);
        editCity = (EditText) findViewById(R.id.editCity);
        editZip = (EditText) findViewById(R.id.editPincode);

        ContentValues values = new ContentValues();
        if (isValid()) {
            values.put("name", editName.getText().toString().trim().equals("")
                    ? "false" : editName.getText().toString());
            values.put("mobile", editMobile.getText().toString().trim().equals("")
                    ? "false" : editMobile.getText().toString());
            values.put("phone", editPhone.getText().toString().trim().equals("")
                    ? "false" : editPhone.getText().toString());
            values.put("email", editEmail.getText().toString().trim().equals("")
                    ? "false" : editEmail.getText().toString());
            values.put("street", editStreet.getText().toString().trim().equals("")
                    ? "false" : editStreet.getText().toString());
            values.put("street2", editStreet2.getText().toString().trim().equals("")
                    ? "false" : editStreet2.getText().toString());
            values.put("website", editWebsite.getText().toString().trim().equals("")
                    ? "false" : editWebsite.getText().toString());
            values.put("fax", editFax.getText().toString().trim().equals("")
                    ? "false" : editFax.getText().toString());
            values.put("city", editCity.getText().toString().trim().equals("")
                    ? "false" : editCity.getText().toString());
            values.put("zip", editZip.getText().toString().trim().equals("")
                    ? "false" : editZip.getText().toString());
            values.put("image_medium", strCustomerImage.equals("false") ? "false" : strCustomerImage);

            partner.update(values, "id = ?", String.valueOf(customer_id));
            Toast.makeText(this, "Customer Updated", Toast.LENGTH_SHORT).show();
            OSyncUtils.get(this, partner).sync(null);
            finish();
        }
    }

    private boolean isValid() {
        editName.setError(null);
        if (editName.getText().toString().trim().isEmpty()) {
            editName.setError(getString(R.string.error_name_required));
            editName.requestFocus();
            return false;
        }
        return true;
    }
}