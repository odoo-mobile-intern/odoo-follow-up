package com.odoo.followup.addons.sales;

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
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.odoo.core.support.CBind;
import com.odoo.followup.R;
import com.odoo.followup.addons.sales.models.ProductProduct;
import com.odoo.followup.orm.OListAdapter;
import com.odoo.followup.orm.data.ListRow;
import com.odoo.followup.utils.BitmapUtils;
import com.odoo.followup.utils.support.BaseFragment;

public class Products extends BaseFragment implements OListAdapter.OnViewBindListener,
        LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    private ProductProduct products;
    private OListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_products, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        products = new ProductProduct(getContext());
        init();
    }

    private void init() {
        adapter = new OListAdapter(getContext(), null, R.layout.sale_product_item_view);
        adapter.setOnViewBindListener(this);
        GridView view = (GridView) findViewById(R.id.productsView);
        view.setAdapter(adapter);
        view.setOnItemClickListener(this);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onViewBind(View view, Cursor cursor, ListRow row) {
        CBind.setText(view.findViewById(R.id.productName), row.getString("name"));
        CBind.setText(view.findViewById(R.id.productPrice), "INR. " + row.getString("list_price"));

        if (!row.getString("image_medium").equals("false")) {
            CBind.setImage(view.findViewById(R.id.productImage),
                    BitmapUtils.getBitmapImage(getContext(), row.getString("image_medium")));
        } else {
            CBind.setImage(view.findViewById(R.id.productImage), R.drawable.no_image);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(), products.getUri(), null, null, null, "name");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.changeCursor(data);
        if (products.isEmpty()) {
            Toast.makeText(getContext(), R.string.getting_data, Toast.LENGTH_LONG).show();
            syncUtils(products).sync(null);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.changeCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Cursor cr = (Cursor) adapter.getItem(position);
        Intent intent = new Intent(getContext(), ProductDetail.class);
        intent.putExtra("id", cr.getInt(cr.getColumnIndex("_id")));
        startActivity(intent);
    }
}
