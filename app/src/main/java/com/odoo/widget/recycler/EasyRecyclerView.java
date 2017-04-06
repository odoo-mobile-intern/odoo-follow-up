package com.odoo.widget.recycler;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class EasyRecyclerView extends RecyclerView {
    public static final String TAG = EasyRecyclerView.class.getSimpleName();
    private Context mContext;
    private LinearLayoutManager linearLayoutManager;
    private GridLayoutManager gridLayoutManager;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private int mLayout = -1;
    private EasyRecyclerViewAdapter adapter;

    public EasyRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public EasyRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EasyRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        linear();
    }

    public EasyRecyclerView linear() {
        linearLayoutManager = new LinearLayoutManager(mContext);
        setLayoutManager(linearLayoutManager);
        return this;
    }

    public GridLayoutManager grid(int spanCount) {
        return grid(spanCount, LinearLayout.VERTICAL, false);
    }

    public GridLayoutManager grid(int spanCount, int orientation) {
        return grid(spanCount, orientation, false);
    }

    public GridLayoutManager grid(int spanCount, int orientation, boolean reversLayout) {
        gridLayoutManager = new GridLayoutManager(mContext, spanCount, orientation, reversLayout);
        setLayoutManager(gridLayoutManager);
        return gridLayoutManager;
    }

    public EasyRecyclerView staggered_grid(int spanCount) {
        return staggered_grid(spanCount, LinearLayout.VERTICAL);
    }

    public EasyRecyclerView staggered_grid(int spanCount, int orientation) {
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(spanCount, orientation);
        setLayoutManager(staggeredGridLayoutManager);
        return this;
    }

    public EasyRecyclerView setLayout(int layout) {
        mLayout = layout;
        return this;
    }

    public void setOnItemViewClickListener(EasyRecyclerViewAdapter.OnItemViewClickListener listener) {
        if (adapter != null) {
            adapter.setOnItemViewClickListener(listener);
        }
    }

    public void setOnViewBindListener(EasyRecyclerViewAdapter.OnViewBindListener listener) {
        if (adapter == null) {
            adapter = new EasyRecyclerViewAdapter(mLayout, null);
            adapter.setOnViewBindListener(listener);
            setAdapter(adapter);
        } else {
            adapter.setOnViewBindListener(listener);
        }
    }

    public void changeCursor(Cursor cursor) {
        if (adapter == null) {
            adapter = new EasyRecyclerViewAdapter(mLayout, cursor);
            setAdapter(adapter);
        } else {
            adapter.changeCursor(cursor);
        }
    }
}