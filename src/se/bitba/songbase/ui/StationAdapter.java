/*
 * Copyright (C) 2014 Bitbase LLC
 *
 * All rights reserved.
 */

package se.bitba.songbase.ui;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.google.common.base.Joiner;
import com.loopj.android.image.SmartImageView;
import se.bitba.songbase.R;
import se.bitba.songbase.provider.SongbaseContract;

public class StationAdapter
        extends CursorAdapter
{
    private static final String TAG = StationAdapter.class.getSimpleName();

    private final Activity activity;

    public StationAdapter(Activity activity, Cursor cursor) {
        super(activity, cursor, 0);
        this.activity = activity;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.d(TAG, String.format("newView(%s, %s, %s)", context, cursor, parent));
        return activity.getLayoutInflater().inflate(R.layout.station_list_item, parent, false);
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

    public interface StationsQuery
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
    }
}
