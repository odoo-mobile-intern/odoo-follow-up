package com.odoo.widget.recycler;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class EasyRecyclerViewAdapter extends
        EasyRecyclerView.Adapter<EasyRecyclerViewAdapter.EasyRecyclerViewHolder> {

    private Cursor mCursor;
    private int mLayout = -1;
    private OnViewBindListener mOnViewBindListener;
    private OnItemViewClickListener mOnItemViewClickListener;

    public EasyRecyclerViewAdapter(int layout_id, Cursor cursor) {
        mLayout = layout_id;
        mCursor = cursor;
    }

    @Override
    public EasyRecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = null;
        Cursor cr = mCursor;
        cr.moveToPosition(i);
        view = inflater.inflate(mLayout, viewGroup, false);
        return new EasyRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EasyRecyclerViewHolder holder, int i) {
        if (mOnViewBindListener != null) {
            Cursor cr = mCursor;
            cr.moveToPosition(i);
            mOnViewBindListener.onViewBind(i, holder.mView, cr);
            if (mOnItemViewClickListener != null) {
                holder.bind(i, cr, mOnItemViewClickListener);
            }
        }
    }

    @Override
    public int getItemCount() {
        return (mCursor != null) ? mCursor.getCount() : 0;
    }

    public void changeCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }


    public class EasyRecyclerViewHolder extends EasyRecyclerView.ViewHolder {
        protected View mView;
        protected OnItemViewClickListener listener;
        protected int position = -1;
        protected Cursor data;

        public EasyRecyclerViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void bind(int position, Cursor data, OnItemViewClickListener listener) {
            this.listener = listener;
            this.position = position;
            this.data = data;
            mView.setTag(this);
            mView.setOnClickListener(clickListener);
        }

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyRecyclerViewHolder holder = (EasyRecyclerViewHolder) v.getTag();
                if (holder.listener != null) {
                    holder.data.moveToPosition(holder.position);
                    holder.listener.onItemViewClick(holder.position, holder.mView, holder.data);
                }
            }
        };
    }

    public EasyRecyclerViewAdapter setOnViewBindListener(OnViewBindListener listener) {
        mOnViewBindListener = listener;
        return this;
    }

    public EasyRecyclerViewAdapter setOnItemViewClickListener(OnItemViewClickListener listener) {
        mOnItemViewClickListener = listener;
        return this;
    }

    public interface OnViewBindListener {
        void onViewBind(int position, View view, Cursor cursor);
    }

    public interface OnItemViewClickListener {
        void onItemViewClick(int position, View view, Cursor cursor);
    }
}