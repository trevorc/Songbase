/*
 * Copyright (C) 2014 Bitbase LLC
 *
 * All rights reserved.
 */

package se.bitba.songbase.ui;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.CursorAdapter;
import android.widget.TextView;
import org.jetbrains.annotations.NotNull;
import se.bitba.songbase.R;
import se.bitba.songbase.provider.SongbaseContract;

public class StationDetailFragment
        extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final String TAG = StationDetailFragment.class.getSimpleName();
    private static final String STATION_ID = "STATION_ID";

    private long mStationId;
    private String mDescription;
    private boolean mIsFavorite;

    public StationDetailFragment() {
    }

    public StationDetailFragment(long stationId) {
        final Bundle arguments = new Bundle();
        arguments.putLong(STATION_ID, stationId);
        setArguments(arguments);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStationId = getArguments().getLong(STATION_ID);

        final Cursor cursor = getActivity().getContentResolver().query(
                SongbaseContract.Station.CONTENT_URI,
                StationQuery.PROJECTION,
                StationQuery.SELECTION,
                new String[]{String.valueOf(mStationId)},
                null);

        assert cursor != null;
        try {
            cursor.moveToNext();
            getActivity().setTitle(cursor.getString(StationQuery.NAME));
            mIsFavorite = cursor.getInt(StationQuery.FAVORITE) != 0;
            mDescription = cursor.getString(StationQuery.DESCRIPTION);
        } finally {
            cursor.close();
        }
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_station_detail, container, false);
        assert view != null;

        final TextView stationDescription = (TextView)view.findViewById(R.id.station_description);
        assert stationDescription != null;

        stationDescription.setText(mDescription);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        getLoaderManager().initLoader(0, null, this);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_station_detail_actions, menu);
        final MenuItem favoriteItem = menu.findItem(R.id.action_favorite);
        favoriteItem.setChecked(mIsFavorite);
        favoriteItem.setIcon(mIsFavorite ?
                                     android.R.drawable.btn_star_big_on :
                                     android.R.drawable.btn_star_big_off);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorite:
                setFavorite(!mIsFavorite);
                getActivity().invalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), SongbaseContract.FeaturedArtist.CONTENT_URI,
                                FeaturedArtistsQuery.PROJECTION, FeaturedArtistsQuery.SELECTION,
                                new String[] {String.valueOf(mStationId)}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, String.format("onLoadFinished() row count is %d", data.getCount()));
        setListAdapter(new FeaturedArtistAdapter(data));
        data.setNotificationUri(getActivity().getContentResolver(),
                                SongbaseContract.FeaturedArtist.CONTENT_URI);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset()");
        setListAdapter(null);
    }

    private void setFavorite(boolean isFavorite) {
        mIsFavorite = isFavorite;
         final ContentValues update = new ContentValues();
        update.put(SongbaseContract.Station.FAVORITE, mIsFavorite);
        if (mIsFavorite) {
            final ContentValues values = new ContentValues();
            values.put(SongbaseContract.FavoriteColumns.STATION_ID, mStationId);
            getActivity().getContentResolver().insert(
                    SongbaseContract.Favorite.CONTENT_URI,
                    values);
        } else {
            getActivity().getContentResolver().delete(
                    SongbaseContract.Favorite.CONTENT_URI,
                    SongbaseContract.Favorite.STATION_ID + "=?",
                    new String[]{String.valueOf(mStationId)});
        }
    }

    private class FeaturedArtistAdapter
            extends CursorAdapter
    {
        public FeaturedArtistAdapter(Cursor cursor) {
            super(getActivity(), cursor, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return getActivity().getLayoutInflater().inflate(
                    android.R.layout.simple_list_item_1, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((TextView)view).setText(cursor.getString(FeaturedArtistsQuery.NAME));
        }
    }

    private interface StationQuery {
        String[] PROJECTION = {
                SongbaseContract.Station.ACTIVITY_ID,
                SongbaseContract.Station.NAME,
                SongbaseContract.Station.DESCRIPTION,
                SongbaseContract.Station.FAVORITE
        };
        String SELECTION = SongbaseContract.Station.STATION_ID + "=?";
        int ACTIVITY_ID = 0;
        int NAME = 1;
        int DESCRIPTION = 2;
        int FAVORITE = 3;
    }

    private interface FeaturedArtistsQuery {
        String[] PROJECTION = {
                SongbaseContract.FeaturedArtistColumns._ID,
                SongbaseContract.FeaturedArtistColumns.NAME
        };
        String SELECTION = SongbaseContract.FeaturedArtistColumns.STATION_ID + "=?";
        int _ID = 0;
        int NAME = 1;
    }
}
