/*
 * Copyright (C) 2014 Bitbase LLC
 *
 * All rights reserved.
 */

package se.bitba.songbase.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import se.bitba.songbase.R;
import se.bitba.songbase.SongbaseConstants;
import se.bitba.songbase.provider.SongbaseContract;

import java.util.Collections;

public class StationDetailActivity
        extends Activity
        implements View.OnClickListener {
    private static final String TAG = StationDetailActivity.class.getSimpleName();

    private Button favoriteButton;

    private String activityId;
    private long stationId;
    private boolean isFavorite;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.station_detail);

        stationId = getIntent().getLongExtra(SongbaseConstants.STATION_ID, -1);
        assert stationId >= 0;
        String description;

        Cursor cursor = getContentResolver().query(
                SongbaseContract.Station.CONTENT_URI,
                StationQuery.PROJECTION, StationQuery.SELECTION,
                new String[]{String.valueOf(stationId)}, null);
        try {
            assert cursor != null;
            cursor.moveToNext();

            setTitle(cursor.getString(StationQuery.NAME));
            activityId = cursor.getString(StationQuery.ACTIVITY_ID);
            isFavorite = cursor.getInt(StationQuery.FAVORITE) != 0;
            description = cursor.getString(StationQuery.DESCRIPTION);
        } finally {
            cursor.close();
        }

        final TextView stationDescription = (TextView)findViewById(R.id.station_description);
        final ListView featuredArtists = (ListView)findViewById(R.id.featured_artists);
        favoriteButton = (Button)findViewById(R.id.favorite_button);
        assert stationDescription != null && featuredArtists != null && favoriteButton != null;

        stationDescription.setText(description);
        featuredArtists.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, Collections.emptyList())); // TODO
        resetFavoriteButtonText();
        favoriteButton.setOnClickListener(this);
    }

    @Override
    public Intent getParentActivityIntent() {
        Intent parentIntent = super.getParentActivityIntent();
        assert parentIntent != null;
        parentIntent.putExtra(SongbaseConstants.ACTIVITY_ID, activityId);
        parentIntent.putExtra(SongbaseConstants.ACTIVITY_NAME, ""); // TODO
        return parentIntent;
    }

    @Override
    public void onClick(View v) {
        isFavorite = !isFavorite;
        resetFavoriteButtonText();
        final ContentValues update = new ContentValues();
        update.put(SongbaseContract.Station.FAVORITE, isFavorite);
        if (isFavorite) {
            final ContentValues values = new ContentValues();
            values.put(SongbaseContract.FavoriteColumns.STATION_ID, stationId);
            getContentResolver().insert(
                    SongbaseContract.Favorite.CONTENT_URI,
                    values);
        } else {
            getContentResolver().delete(
                    SongbaseContract.Favorite.CONTENT_URI,
                    SongbaseContract.Favorite.STATION_ID + "=?",
                    new String[]{String.valueOf(stationId)});
        }
    }

    private void resetFavoriteButtonText() {
        favoriteButton.setText(isFavorite ? R.string.clear_favorite : R.string.save_favorite);
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
}
