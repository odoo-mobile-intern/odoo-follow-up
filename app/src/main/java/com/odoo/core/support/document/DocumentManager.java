package com.odoo.core.support.document;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import com.odoo.core.support.OdooActivity;
import com.odoo.followup.R;
import com.odoo.followup.orm.data.ListRow;
import com.odoo.followup.utils.BitmapUtils;

import java.io.File;

public class DocumentManager implements OdooActivity.OnOdooActivityResultListener {

    private static final int REQUEST_ALL_FILES = 1;
    private OdooActivity mOdooActivity;
    private OnFileSelectListener mOnFileSelectListener;


    private DocumentManager(OdooActivity activity) {
        mOdooActivity = activity;
        mOdooActivity.setOnActivityResultListener(this);
    }

    public static DocumentManager getInstance(OdooActivity activity) {
        return new DocumentManager(activity);
    }


    public void requestFile(OnFileSelectListener callback) {
        mOnFileSelectListener = callback;
        requestFiles();
    }

    private void requestFiles() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mOdooActivity.startActivityForResult(Intent.createChooser(intent, getString(R.string.label_select_file)),
                REQUEST_ALL_FILES);
    }

    private String getString(int res_id) {
        return mOdooActivity.getString(res_id);
    }

    @Override
    public void onOdooActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_ALL_FILES:
                    ListRow fileData = getDataFromUri(data.getData());
                    if (mOnFileSelectListener != null)
                        mOnFileSelectListener.onFileSelected(fileData);
                    break;
            }
        } else {
            if (mOnFileSelectListener != null) {
                mOnFileSelectListener.onFileSelectCancel();
            }
        }
    }

    private ListRow getDataFromUri(Uri uri) {
        ListRow fileData = new ListRow();
        if (uri.getScheme().equalsIgnoreCase("content")) {
            // URI Content
            ListRow uriData = getData(uri, null, null);
            if (uriData != null) {
                fileData.put("content_uri", uri.toString());
                fileData.put("datas_fname", uriData.getString(OpenableColumns.DISPLAY_NAME));
                fileData.put("name", fileData.getString("datas_fname"));
                fileData.put("file_size", uriData.getLong(OpenableColumns.SIZE));
                fileData.put("file_path", getPath(uri));
                fileData.put("datas", getBase64Data(fileData.getString("file_path")));
            }
        } else if (uri.getScheme().equals("file")) {

        }
        return fileData;
    }

    private String getBase64Data(String file_path) {
        if (file_path != null)
            return BitmapUtils.uriToBase64(Uri.fromFile(new File(file_path)),
                    mOdooActivity.getContentResolver(), false);
        return "false";
    }

    private String getPath(Uri uri) {

        if (uri.getScheme().equalsIgnoreCase("content") &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String document_id = DocumentsContract.getDocumentId(uri);
            if (isMediaDocument(uri)) {
                String[] documentData = document_id.split(":");
                Uri requestURI = null;
                switch (documentData[0]) {
                    case "image":
                        requestURI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        break;
                    case "video":
                        requestURI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        break;
                    case "audio":
                        requestURI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        break;
                }
                if (requestURI != null) {
                    ListRow data = getData(requestURI, "_id = ?", new String[]{documentData[1]});
                    if (data != null)
                        return data.getString(MediaStore.MediaColumns.DATA);
                }
            }

        } else {
            ContentResolver mCR = mOdooActivity.getContentResolver();
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = mCR.query(uri, projection, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String filePath = cursor.getString(column_index);
                cursor.close();
                return filePath;
            }
        }
        return null;
    }

    private ListRow getData(Uri uri, String where, String[] args) {
        ContentResolver contentResolver = mOdooActivity.getContentResolver();
        Cursor cr = contentResolver.query(uri, null, where, args, null, null);
        if (cr != null && cr.moveToFirst()) {
            return new ListRow(cr);
        }
        return null;
    }

    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }

    public interface OnFileSelectListener {
        void onFileSelected(ListRow fileData);

        void onFileSelectCancel();
    }
}
