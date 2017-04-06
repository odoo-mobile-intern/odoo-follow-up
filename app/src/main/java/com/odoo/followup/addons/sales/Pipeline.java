package com.odoo.followup.addons.sales;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.odoo.core.support.CBind;
import com.odoo.followup.R;
import com.odoo.followup.addons.sales.models.CRMLead;
import com.odoo.followup.addons.sales.models.CrmStage;
import com.odoo.followup.orm.OListAdapter;
import com.odoo.followup.orm.data.ListRow;
import com.odoo.followup.orm.sync.OSyncUtils;
import com.odoo.followup.utils.BitmapUtils;
import com.odoo.followup.utils.support.BaseFragment;
import com.odoo.widget.kanban.CardItem;
import com.odoo.widget.kanban.CardPagerAdapter;
import com.odoo.widget.kanban.ShadowTransformer;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;

public class Pipeline extends BaseFragment implements
        OListAdapter.OnViewBindListener, LoaderManager.LoaderCallbacks<Cursor> {

    private CRMLead lead;
    private CrmStage stageObj;
    private CardPagerAdapter mCardAdapter;
    private ViewPager viewPager;

    private HashMap<String, ListRow> stages = new HashMap<>();
    private HashMap<String, OListAdapter> adapters = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pipeline, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lead = new CRMLead(getContext());
        stageObj = new CrmStage(getContext());
        init();
    }

    private void init() {
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        mCardAdapter = new CardPagerAdapter() {

            @Override
            public View getView(ViewGroup container, int position, CardItem item) {
                if (item.data != null)
                    return super.getView(container, position, item);
                return LayoutInflater.from(getContext()).inflate(R.layout.sale_pipeline_item_view, container, false);
            }

            @Override
            public void bind(CardItem item, View view) {
                CBind.setText(view.findViewById(R.id.stageName), item.title);
                if (item.data != null) {
                    OListAdapter adapter = getAdapter(view, item.data);
                    adapters.put("adapter_" + item.data.getInt("_id"), adapter);
                    getLoaderManager().initLoader(item.data.getInt("_id"), null, Pipeline.this);
                }
            }
        };


        for (ListRow stage : stageObj.select(null, "sequence", null)) {
            mCardAdapter.addCardItem(new CardItem(stage.getString("name"), stage));
            stages.put("stage_" + stage.getInt("_id"), stage);
        }
        ShadowTransformer mCardShadowTransformer = new ShadowTransformer(viewPager, mCardAdapter);
        viewPager.setAdapter(mCardAdapter);
        viewPager.setPageTransformer(false, mCardShadowTransformer);
        viewPager.setOffscreenPageLimit(3);
    }

    private OListAdapter getAdapter(View view, ListRow stage) {
        GridView gridView = (GridView) view.findViewById(R.id.pagerGridView);
        final OListAdapter adapter = new OListAdapter(getContext(), null, R.layout.sale_pipeline_item_view) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.sale_pipeline_item_view, parent, false);
                }
                ListRow row = new ListRow(lead, (Cursor) getItem(position));
                CBind.setText(convertView.findViewById(R.id.leadName), row.getString("name"));
                CBind.setText(convertView.findViewById(R.id.planned_revenue), getCurrency(row.getDouble("planned_revenue")));
                switch (row.getString("kanban_state")) {
                    case "grey":
                        convertView.findViewById(R.id.kanban_state).setBackgroundColor(Color.parseColor("#AEAEAE"));
                        break;
                    case "green":
                        convertView.findViewById(R.id.kanban_state).setBackgroundColor(Color.parseColor("#5CB85C"));
                        break;
                    case "red":
                        convertView.findViewById(R.id.kanban_state).setBackgroundColor(Color.parseColor("#D9534F"));
                        break;
                }
                ListRow user = row.getM2O("user_id");
                String image = "false";
                if (user != null) {
                    image = row.getM2O("user_id").getString("image_medium");
                }
                if (image.equals("false")) {
                    CBind.setImage(convertView.findViewById(R.id.assigneeAvatar), R.drawable.user_profile);
                } else {
                    CBind.setImage(convertView.findViewById(R.id.assigneeAvatar), BitmapUtils.getBitmapImage(getContext(), image));
                }
                return convertView;
            }
        };
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ListRow row = new ListRow((Cursor) adapter.getItem(i));
                Intent intent = new Intent(getContext(), PipelineDetail.class);
                intent.putExtra("_id", row.getInt("_id"));
                startActivity(intent);
            }
        });
        return adapter;
    }

    private String getCurrency(double number) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(number);
    }

    @Override
    public void onViewBind(View view, Cursor cursor, ListRow row) {
        CBind.setText(view.findViewById(R.id.leadName), row.getString("name"));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(), lead.getUri(), null, "stage_id = ?", new String[]{id + ""}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapters.get("adapter_" + loader.getId()).changeCursor(data);
        if (lead.isEmpty()) {
            Toast.makeText(getContext(), R.string.getting_data, Toast.LENGTH_LONG).show();
            syncUtils(lead).sync(null);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapters.get("adapter_" + loader.getId()).changeCursor(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        OSyncUtils.get(getContext(), lead).sync(new Bundle());
        OSyncUtils.get(getContext(), stageObj).sync(new Bundle());
        OSyncUtils.onSyncFinishListener(parent(), new OSyncUtils.OnSyncFinishListener() {
            @Override
            public void onSyncFinish(String model) {
                if (model.equals(stageObj.getModelName())) {
                    viewPager.removeAllViews();
                    adapters.clear();
                    stages.clear();
                    init();
                }
            }
        });
    }
}
