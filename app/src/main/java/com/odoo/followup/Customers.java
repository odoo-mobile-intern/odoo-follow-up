package com.odoo.followup;

import android.content.Intent;
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

import com.odoo.core.support.CBind;
import com.odoo.followup.addons.customers.NewCustomer;
import com.odoo.followup.orm.OListAdapter;
import com.odoo.followup.orm.data.ListRow;
import com.odoo.followup.orm.models.ResPartner;
import com.odoo.followup.utils.BitmapUtils;

public class Customers extends BaseFragment implements OListAdapter.OnViewBindListener,
        LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {
    private ResPartner contacts;
    private OListAdapter listAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasFABButton(true);
        setOnFABClick(this);
        contacts = new ResPartner(getContext());
        ListView contactList = (ListView) view.findViewById(R.id.contactList);
        listAdapter = new OListAdapter(getContext(), null, R.layout.partner_list_item);
        contactList.setAdapter(listAdapter);
        listAdapter.setOnViewBindListener(this);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onViewBind(View view, Cursor cursor, ListRow row) {

        String strEmail = row.getString("email");
        String strCity = row.getString("city");

        CBind.setText(view.findViewById(R.id.text_partner_name), row.getString("name"));
        CBind.setText(view.findViewById(R.id.text_email), strEmail);
        CBind.setText(view.findViewById(R.id.text_city), strCity);

        view.findViewById(R.id.text_email).setVisibility(strEmail.equals("false")
                ? View.GONE : View.VISIBLE);

        view.findViewById(R.id.text_city).setVisibility(strCity.equals("false")
                ? View.GONE : View.VISIBLE);

        if (row.getString("image_medium").equals("false"))
            CBind.setImage(view.findViewById(R.id.avatar), BitmapUtils.getAlphabetImage(getContext(),
                    row.getString("name")));
        else
            CBind.setImage(view.findViewById(R.id.avatar), BitmapUtils.getBitmapImage(getContext(),
                    row.getString("image_medium")));

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(), contacts.getUri(), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        listAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        listAdapter.changeCursor(null);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                startActivity(new Intent(getContext(), NewCustomer.class));
                break;
        }

    }
}
