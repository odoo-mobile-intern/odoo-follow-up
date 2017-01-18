package com.odoo.followup;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.odoo.followup.orm.OListAdapter;
import com.odoo.followup.orm.data.ListRow;
import com.odoo.followup.orm.models.ResPartner;
import com.odoo.followup.orm.sync.OSyncUtils;
import com.odoo.followup.utils.BitmapUtils;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener,
        OListAdapter.OnViewBindListener, LoaderManager.LoaderCallbacks<Cursor> {

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private OListAdapter listAdapter;
    private ResPartner partner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();
    }

    private void init() {
        partner = new ResPartner(this);

        ListView partnerList = (ListView) findViewById(R.id.partner_list);
        listAdapter = new OListAdapter(this, null, R.layout.partner_list_item);
        listAdapter.setOnViewBindListener(this);
        partnerList.setAdapter(listAdapter);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        findViewById(R.id.fab).setOnClickListener(this);
        setUpDrawer();

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.fab) {
        }
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
        int id = item.getItemId();
        if (id == R.id.menu_refresh) {
            OSyncUtils.get(this, partner).sync(null);
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onViewBind(View view, Cursor cursor, ListRow row) {
        TextView textPartnerName, textEmail, textCity;
        ImageView partnerImage;

        textPartnerName = (TextView) view.findViewById(R.id.text_partner_name);
        textEmail = (TextView) view.findViewById(R.id.text_email);
        textCity = (TextView) view.findViewById(R.id.text_city);
        partnerImage = (ImageView) view.findViewById(R.id.avatar);

        textPartnerName.setText(row.getString("name"));
        if (!row.getString("image_medium").equals("false"))
            partnerImage.setImageBitmap(BitmapUtils.getBitmapImage(this, row.getString("image_medium")));
        else
            partnerImage.setImageBitmap(BitmapUtils.getAlphabetImage(this, row.getString("image_medium")));

        textEmail.setText(row.getString("email"));
        textEmail.setVisibility(row.getString("email").equals("false") ? View.GONE : View.VISIBLE);

        textCity.setText(row.getString("city"));
        textCity.setVisibility(row.getString("city").equals("false") ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, partner.getUri(), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        listAdapter.changeCursor(data);
        if (partner.count() <= 0) {
            OSyncUtils.get(this, partner).sync(null);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        listAdapter.changeCursor(null);
    }
}
