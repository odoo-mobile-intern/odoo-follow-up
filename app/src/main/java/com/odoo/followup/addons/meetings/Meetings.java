package com.odoo.followup.addons.meetings;

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
import android.widget.Toast;

import com.odoo.core.support.CBind;
import com.odoo.followup.R;
import com.odoo.followup.addons.meetings.models.CalendarEvent;
import com.odoo.followup.orm.OListAdapter;
import com.odoo.followup.orm.data.ListRow;
import com.odoo.followup.utils.support.BaseFragment;

public class Meetings extends BaseFragment implements OListAdapter.OnViewBindListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private CalendarEvent calendarEvent;
    private OListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meetings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        calendarEvent = new CalendarEvent(getContext());

        ListView meetingList = (ListView) view.findViewById(R.id.meetingList);
        adapter = new OListAdapter(getContext(), null, R.layout.meeting_list_item);
        adapter.setOnViewBindListener(this);
        meetingList.setAdapter(adapter);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onViewBind(View view, Cursor cursor, ListRow row) {
        CBind.setText(view.findViewById(R.id.textMeetingName), row.getString("name"));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(), calendarEvent.getUri(), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.changeCursor(data);
        if (calendarEvent.isEmpty()) {
            Toast.makeText(getContext(), R.string.getting_data, Toast.LENGTH_LONG).show();
            syncUtils(calendarEvent).sync(null);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.changeCursor(null);
    }
}
