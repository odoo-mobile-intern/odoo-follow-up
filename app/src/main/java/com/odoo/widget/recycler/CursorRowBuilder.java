package com.odoo.widget.recycler;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CursorRowBuilder {

    private String[] columns;
    private MatrixCursor matrixCursor;

    public CursorRowBuilder(String[] cols) {
        columns = cols;
        matrixCursor = new MatrixCursor(cols);
    }

    public CursorRowBuilder addRow(String[] values) {
        matrixCursor.addRow(values);
        return this;
    }

    public Cursor get() {
        return matrixCursor;
    }

    public Cursor getWithCursors(Cursor... cr) {
        List<Cursor> cursors = new ArrayList<>();
        cursors.add(get());
        cursors.addAll(Arrays.asList(cr));
        return new MergeCursor(cursors.toArray(new Cursor[cursors.size()]));
    }
}