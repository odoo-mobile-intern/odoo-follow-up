package com.odoo.widget.chatter;

import android.app.Activity;
import android.app.DownloadManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import com.odoo.core.support.CBind;
import com.odoo.core.support.OUser;
import com.odoo.core.support.OdooActivity;
import com.odoo.core.utils.OAppBarUtils;
import com.odoo.followup.R;
import com.odoo.followup.orm.OListAdapter;
import com.odoo.followup.orm.data.ListRow;
import com.odoo.followup.orm.models.IrAttachment;

import java.util.Locale;

public class AttachmentViewer extends OdooActivity implements OListAdapter.OnViewBindListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String KEY_RECORD_NAME = "record_name";
    public static final String KEY_MODEL_NAME = "model_name";
    public static final String KEY_RECORD_ID = "record_id";
    private Bundle extra;
    private IrAttachment irAttachment;
    private OListAdapter adapter;
    private OUser mUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attachment_viewer);
        OAppBarUtils.setAppBar(this, true);
        extra = getIntent().getExtras();
        irAttachment = new IrAttachment(this);
        mUser = OUser.current(this);
        setTitle(extra.getString(KEY_RECORD_NAME));
        ActionBar toolbar = getSupportActionBar();
        assert toolbar != null;
        toolbar.setSubtitle(getString(R.string.title_attachments));
        bindGrid();
    }

    private void bindGrid() {
        adapter = new OListAdapter(this, null, R.layout.chatter_attachment_item_view);
        adapter.setOnViewBindListener(this);
        GridView gridView = (GridView) findViewById(R.id.recordAttachmentsGrid);
        getSupportLoaderManager().initLoader(0, null, this);
        gridView.setAdapter(adapter);
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

    @Override
    public void onViewBind(View view, Cursor cursor, final ListRow row) {
        CBind.setText(view.findViewById(R.id.attachmentFileName), row.getString("name"));
        CBind.setText(view.findViewById(R.id.attachmentFileSize), String.format(Locale.getDefault(),
                "%.2f MB", row.getInt("file_size") / 1024.0F));
        view.findViewById(R.id.downloadAttachment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadFile(row.getInt("id"), row.getString("name"));
                Toast.makeText(AttachmentViewer.this, "Downloading " + row.getString("name"), Toast.LENGTH_SHORT).show();
            }
        });

        CBind.setImage(view.findViewById(R.id.attachmentPreview),
                row.getString("mimetype").contains("image") ? R.drawable.img_image : R.drawable.img_document);
    }

    public void downloadFile(int attachment_id, String fileName) {

        String file_url = mUser.getHost() + "/web/content/" + attachment_id + "?download=true";

        // Removing last semicolon from filename
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(file_url));
        request.addRequestHeader("Connection", "keep-alive");
        request.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        request.addRequestHeader("Cookie", "session_id=" + mUser.getSession_id());
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        DownloadManager dm = (DownloadManager) getSystemService(Activity.DOWNLOAD_SERVICE);
        dm.enqueue(request);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String where = "res_model = ? and res_id = ?";
        String[] vals = {extra.getString(KEY_MODEL_NAME), extra.getInt(KEY_RECORD_ID) + ""};
        return new CursorLoader(this, irAttachment.getUri(), null, where, vals, "write_date DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.changeCursor(null);
    }
}
