package com.odoo.followup;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.odoo.core.support.CBind;
import com.odoo.core.support.OUser;
import com.odoo.core.support.OdooActivity;
import com.odoo.followup.addons.customers.Customers;
import com.odoo.followup.addons.dashboard.Dashboard;
import com.odoo.followup.addons.meetings.Meetings;
import com.odoo.followup.addons.sales.NextActivity;
import com.odoo.followup.addons.sales.Pipeline;
import com.odoo.followup.addons.sales.Products;
import com.odoo.followup.utils.BitmapUtils;

public class HomeActivity extends OdooActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private OUser user;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        user = OUser.current(this);
        // loading default fragment
        startFragment(new Dashboard(), "Dashboard");
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    public void setFabVisible(boolean visible) {
        fab.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void init() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        if (user.getAvatar().equals("false"))
            CBind.setImage(header.findViewById(R.id.userAvatar), R.drawable.user_profile);
        else
            CBind.setImage(header.findViewById(R.id.userAvatar), BitmapUtils.getBitmapImage(this, user.getAvatar()));
        CBind.setText(header.findViewById(R.id.userName), user.getName());
        CBind.setText(header.findViewById(R.id.userHost), user.getHost());
        setUpDrawer();
    }

    private void setUpDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_dashboard:
                startFragment(new Dashboard(), "Dashboard");
                break;
            case R.id.menu_customer:
                startFragment(new Customers(), "Customer");
                break;
            case R.id.menu_pipeline:
                startFragment(new Pipeline(), "Pipeline");
                break;
            case R.id.menu_next_activity:
                startFragment(new NextActivity(), "Next Activity");
                break;
            case R.id.menu_products:
                startFragment(new Products(), "Products");
                break;
            case R.id.menu_meeting:
                startFragment(new Meetings(), "Meetings");
                break;
            case R.id.menu_profile:
                startActivity(new Intent(this, UserProfile.class));
                break;
            case R.id.menu_settings:
                startActivity(new Intent(this, Settings.class));
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void startFragment(Fragment fragment, String title, boolean backState) {
        setTitle(title);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment);
        if (backState) {
            transaction.addToBackStack(fragment.getClass().getCanonicalName());
        }
        transaction.commit();
    }

    public void startFragment(Fragment fragment, String title) {
        startFragment(fragment, title, false);
    }
}
