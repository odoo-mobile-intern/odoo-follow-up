package com.odoo.followup.addons.sales;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.odoo.core.support.CBind;
import com.odoo.core.support.OUser;
import com.odoo.followup.R;
import com.odoo.followup.addons.sales.models.ProductTemplate;
import com.odoo.followup.orm.data.ListRow;
import com.odoo.followup.utils.BitmapUtils;

import java.util.List;

public class ProductDetail extends AppCompatActivity {

    private ProductTemplate product;
    private String productWebsiteURL;
    private int product_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        product_id = getIntent().getIntExtra("id", 0);
        productWebsiteURL = OUser.current(this).getHost() + "/shop/product/" + product_id;

        Toolbar toolbar = (Toolbar) findViewById(R.id.product_detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        product = new ProductTemplate(this);
        setProductDetail();
    }

    private void setProductDetail() {
        List<ListRow> rows = product.select("id = ? ", new String[]{String.valueOf(product_id)});
        for (ListRow row : rows) {
            setTitle(row.getString("name"));

            if (!row.getString("image_medium").equals("false")) {
                CBind.setImage(findViewById(R.id.product_image),
                        BitmapUtils.getBitmapImage(this, row.getString("image_medium")));
            } else {
                CBind.setImage(findViewById(R.id.product_image), R.drawable.no_image);
            }

            CBind.setText(findViewById(R.id.product_name), row.getString("name"));
            CBind.setText(findViewById(R.id.product_price), row.getString("list_price"));
            CBind.setText(findViewById(R.id.product_desc), row.getString("description_sale"));

            findViewById(R.id.desc_card_view).setVisibility(
                    row.getString("description_sale").equals("false") ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.product_detail_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_product_share:
                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, productWebsiteURL);
                startActivity(Intent.createChooser(shareIntent, "Share using..."));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
