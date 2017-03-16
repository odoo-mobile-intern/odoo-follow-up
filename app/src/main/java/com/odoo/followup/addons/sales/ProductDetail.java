package com.odoo.followup.addons.sales;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ProductDetail extends AppCompatActivity {

    private ProductTemplate product;
    private String productWebsiteURL, productName, productPrice;
    private int product_id;
    private boolean isProductImage;

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
            productName = row.getString("name");
            productPrice = row.getString("list_price");
            setTitle(productName);

            if (!row.getString("image_medium").equals("false")) {
                CBind.setImage(findViewById(R.id.product_image),
                        BitmapUtils.getBitmapImage(this, row.getString("image_medium")));
            } else {
                CBind.setImage(findViewById(R.id.product_image), R.drawable.no_image);
            }
            isProductImage = saveToCache(row.getString("image_medium"));

            CBind.setText(findViewById(R.id.product_name), row.getString("name"));
            CBind.setText(findViewById(R.id.product_price), productPrice);
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
                shareProductDetail();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareProductDetail() {
        String productDetail = "Name: " + productName + "\n"
                + "Price:  INR. " + productPrice + "\n"
                + "Link: " + productWebsiteURL;
        File imagePath = new File(getCacheDir(), "images");
        File newFile = new File(imagePath, "image.png");
        Uri contentUri = FileProvider.getUriForFile(this, "com.odoo.followup.fileprovider", newFile);
        Intent shareIntent = new Intent();
        if (contentUri != null) {
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
            if (isProductImage) {
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            }
            shareIntent.putExtra(Intent.EXTRA_TEXT, productDetail);
            startActivity(Intent.createChooser(shareIntent, "Choose an app"));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private boolean saveToCache(String base64) {
        try {
            if (!base64.equals("false")) {
                Bitmap productImage = BitmapUtils.getBitmapImage(this, base64);
                File cachePath = new File(getCacheDir(), "images");
                cachePath.mkdirs();
                FileOutputStream stream = new FileOutputStream(cachePath + "/image.png");
                productImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.close();
                return true;
            } else return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
