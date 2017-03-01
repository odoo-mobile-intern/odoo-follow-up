package com.odoo.followup.addons.customers;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.odoo.followup.R;
import com.odoo.followup.addons.customers.models.ResPartner;
import com.odoo.followup.orm.sync.OSyncUtils;
import com.odoo.followup.utils.BitmapUtils;

public class NewCustomer extends AppCompatActivity implements View.OnClickListener {

    private ImageView avatar;
    private EditText editName, editMobileNumber, editPhoneNumber, editEmail, editStreet, editStreet2,
            editCity, editPincode, editFax, editWebsite;
    private CheckBox checkBoxIsCompany;
    private ResPartner partner;
    private String profileImageString = "null";

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_partner);

        partner = new ResPartner(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.title_new_contact);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();
    }

    private void init() {

        avatar = (ImageView) findViewById(R.id.avatar);
        avatar.setOnClickListener(this);

        editName = (EditText) findViewById(R.id.editName);
        editEmail = (EditText) findViewById(R.id.editEmail);
        editMobileNumber = (EditText) findViewById(R.id.editMobileNumber);
        editPhoneNumber = (EditText) findViewById(R.id.editPhoneNumber);
        editEmail = (EditText) findViewById(R.id.editEmail);
        editStreet = (EditText) findViewById(R.id.editStreet);
        editStreet2 = (EditText) findViewById(R.id.editStreet2);
        editCity = (EditText) findViewById(R.id.editCity);
        editPincode = (EditText) findViewById(R.id.editPincode);
        editWebsite = (EditText) findViewById(R.id.editWebsite);
        editFax = (EditText) findViewById(R.id.editFax);
        checkBoxIsCompany = (CheckBox) findViewById(R.id.checkboxIsCompany);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_contact_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_contact_save:
                SaveContact();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void SaveContact() {
        if (validName()) {
            ContentValues values = new ContentValues();
            values.put("name", editName.getText().toString());

            if (checkBoxIsCompany.isChecked()) {
                values.put("company_type", "company");
            } else {
                values.put("company_type", "person");
            }

            if (editMobileNumber.getText().toString().equals("")) {
                values.put("mobile", "false");
            } else {
                values.put("mobile", editMobileNumber.getText().toString());
            }

            if (editPhoneNumber.getText().toString().equals("")) {
                values.put("phone", "false");
            } else {
                values.put("phone", editPhoneNumber.getText().toString());
            }

            if (editCity.getText().toString().equals("")) {
                values.put("city", "false");
            } else {
                values.put("city", editCity.getText().toString());
            }

            if (editStreet.getText().toString().equals("")) {
                values.put("street", "false");
            } else {
                values.put("street", editStreet.getText().toString());
            }

            if (editStreet2.getText().toString().equals("")) {
                values.put("street2", "false");
            } else {
                values.put("street2", editStreet2.getText().toString());
            }

            if (editEmail.getText().toString().equals("")) {
                values.put("email", "false");
            } else {
                values.put("email", editEmail.getText().toString());
            }

            if (editWebsite.getText().toString().equals("")) {
                values.put("website", "false");
            } else {
                values.put("website", editWebsite.getText().toString());
            }

            if (editFax.getText().toString().equals("")) {
                values.put("fax", "false");
            } else {
                values.put("fax", editFax.getText().toString());
            }

            if (editPincode.getText().toString().equals("")) {
                values.put("zip", "false");
            } else {
                values.put("zip", editPincode.getText().toString());
            }

            if (profileImageString.equals("") || profileImageString.equals("null")) {
                values.put("image_medium", "false");
            } else {
                values.put("image_medium", profileImageString);
            }

            partner.create(values);
            OSyncUtils.get(this, partner).sync(new Bundle());
            Toast.makeText(this, "Contact Saved", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private boolean validName() {
        if (editName.getText().toString().trim().isEmpty()) {
            editName.setError("Enter Name");
            editName.setFocusable(true);
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        selectImageFromDevice();
    }

    private void selectImageFromDevice() {
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
                    "Select Image from"), 1);

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
                    avatar.setImageBitmap(bitmap);
                    profileImageString = BitmapUtils.bitmapToBase64(bitmap);
                }
            } else {
                NavUtils.getParentActivityIntent(this);
            }
        }
    }
}
