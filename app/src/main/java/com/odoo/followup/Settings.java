package com.odoo.followup;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.odoo.core.support.OdooActivity;
import com.odoo.core.utils.OAppBarUtils;
import com.odoo.followup.addons.customers.call.OverlayPermissionManager;

public class Settings extends OdooActivity {

    private Switch callerWindowSwitch, overlaySwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        OAppBarUtils.setAppBar(this, true);
        setTitle(R.string.action_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        checkOverlayPermission();

        callerWindowSwitch = (Switch) findViewById(R.id.callerWindowSwitch);
        overlaySwitch = (Switch) findViewById(R.id.overlaySwitch);

        callerWindowSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

            }
        });

        overlaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

            }
        });
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

    private void checkOverlayPermission() {
        // TODO, before going to take permission, ask user to allow and than redirect to settings.

        OverlayPermissionManager permissionManager = new OverlayPermissionManager(this);
        permissionManager.checkDrawOverlayPermission(new OverlayPermissionManager.OnOverlayPermissionListener() {
            @Override
            public void canDrawerOverlays(boolean canDraw) {
                Log.v("OdooFollowUp", "Draw overlays " + canDraw);
                //TODO: Move to settings (preference)
            }
        });
    }

}
