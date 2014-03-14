/*
 * Copyright (C) 2014 Bitbase LLC
 *
 * All rights reserved.
 */

package se.bitba.songbase.ui;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.TextView;
import se.bitba.songbase.fetch.FetchManager;
import se.bitba.songbase.provider.SongbaseContract;

public class ActivityListFragment
        extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener
{
    private static final String TAG = ActivityListFragment.class.getSimpleName();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated()");
        getLoaderManager().initLoader(0, null, this);
        new FetchManager(getActivity()).fetchActivities();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), SongbaseContract.Activity.CONTENT_URI,
                                ActivitiesQuery.PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, String.format("onLoadFinished() row count is %d", data.getCount()));
        final Context context = getActivity();
        if (context == null) throw new IllegalStateException("getActivity() returned null");
        setListAdapter(new ActivityAdapter(getActivity(), data));
        getListView().setOnItemClickListener(this);
        data.setNotificationUri(getActivity().getContentResolver(),
                                SongbaseContract.Activity.CONTENT_URI);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        setListAdapter(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (getListAdapter() == null) return;
        final Cursor cursor = ((CursorAdapter)getListAdapter()).getCursor();
        assert cursor != null;

        cursor.moveToPosition(position);
        final String activityId = cursor.getString(ActivitiesQuery.ACTIVITY_ID);
        final Uri uri = SongbaseContract.Activity.buildActivityUri(activityId);
        Log.d(TAG, String.format("startActivity(%s)", uri));
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    private static class ActivityAdapter
            extends CursorAdapter
    {
        public ActivityAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(
                    android.R.layout.simple_list_item_1, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((TextView)view).setText(cursor.getString(ActivitiesQuery.NAME));
        }
    }

    private interface ActivitiesQuery
    {
        String[] PROJECTION = {
                SongbaseContract.Activity._ID,
                SongbaseContract.Activity.ACTIVITY_ID,
                SongbaseContract.Activity.NAME
        };

        int _ID = 0;
        int ACTIVITY_ID = 1;
        int NAME = 2;
    }
}
