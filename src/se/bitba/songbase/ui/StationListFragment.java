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
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.google.common.base.Joiner;
import com.loopj.android.image.SmartImageView;
import se.bitba.songbase.R;
import se.bitba.songbase.fetch.FetchManager;
import se.bitba.songbase.provider.SongbaseContract;

public class StationListFragment
        extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener
{
    private static final String TAG = StationListFragment.class.getSimpleName();
    private static final String ACTIVITY_ID = "ACTIVITY_ID";

    public StationListFragment(String activityId) {
        Bundle arguments = new Bundle();
        arguments.putString(ACTIVITY_ID, activityId);
        setArguments(arguments);
    }

    private String activityId;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated()");

        activityId = getArguments().getString(ACTIVITY_ID);

        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        getLoaderManager().initLoader(0, null, this);
        new FetchManager(getActivity()).fetchStations(activityId);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), SongbaseContract.Station.CONTENT_URI,
                                StationsQuery.PROJECTION, StationsQuery.SELECTION,
                                new String[] {activityId}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        setListAdapter(new StationAdapter(getActivity(), data));
        getListView().setOnItemClickListener(this);
        data.setNotificationUri(getActivity().getContentResolver(),
                                SongbaseContract.Station.CONTENT_URI);
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
        final long stationId = cursor.getLong(StationsQuery.STATION_ID);
        final Uri uri = SongbaseContract.Station.buildStationUri(stationId);
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    private static class StationAdapter
            extends CursorAdapter
    {
        private static final String TAG = StationAdapter.class.getSimpleName();

        public StationAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            Log.d(TAG, String.format("newView(%s, %s, %s)", context, cursor, parent));
            return LayoutInflater.from(context).inflate(R.layout.list_item_station, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            Log.d(TAG, String.format("getView(%s, %s, %s)", view, context, cursor));

            final SmartImageView imageView = (SmartImageView)view.findViewById(R.id.station_item_cover);
            final TextView nameView = (TextView)view.findViewById(R.id.station_item_name);
            final TextView descriptionView = (TextView)view.findViewById(R.id.station_item_description);
            assert imageView != null && nameView != null && descriptionView != null;

            imageView.setImageUrl(cursor.getString(StationsQuery.COVER_URL));
            nameView.setText(cursor.getString(StationsQuery.NAME));
            descriptionView.setText(cursor.getString(StationsQuery.DESCRIPTION));
            view.setBackgroundColor(cursor.getInt(StationsQuery.FAVORITE) == 0 ? Color.TRANSPARENT : Color.YELLOW);
        }
    }

    private interface StationsQuery
    {
        String[] PROJECTION = {
                SongbaseContract.Station._ID,
                SongbaseContract.Station.STATION_ID,
                SongbaseContract.Station.NAME,
                SongbaseContract.Station.COVER_URL,
                SongbaseContract.Station.DESCRIPTION,
                SongbaseContract.Station.FAVORITE
        };
        String SELECTION = Joiner.on(" AND ").join(
                SongbaseContract.StationColumns.NAME + " IS NOT NULL",
                SongbaseContract.StationColumns.ACTIVITY_ID + "=?");

        int _ID = 0;
        int STATION_ID = 1;
        int NAME = 2;
        int COVER_URL = 3;
        int DESCRIPTION = 4;
        int FAVORITE = 5;
    }}
