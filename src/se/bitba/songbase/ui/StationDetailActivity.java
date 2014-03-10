/*
 * Copyright (C) 2014 Bitbase LLC
 *
 * All rights reserved.
 */

package se.bitba.songbase.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import se.bitba.songbase.R;
import se.bitba.songbase.SongbaseConstants;
import se.bitba.songbase.model.SongzaActivity;
import se.bitba.songbase.model.SongzaStation;

public class StationDetailActivity
        extends Activity
        implements View.OnClickListener
{
    private static final String TAG = StationDetailActivity.class.getSimpleName();

    private TextView stationDescription;
    private ListView featuredArtists;
    private Button favoriteButton;

    private SongzaActivity songzaActivity;
    private SongzaStation songzaStation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.station_detail);

        songzaActivity = getIntent().getParcelableExtra(SongbaseConstants.ACTIVITY);
        songzaStation = getIntent().getParcelableExtra(SongbaseConstants.STATION);
        assert songzaActivity != null && songzaStation != null;

        stationDescription =(TextView)findViewById(R.id.station_description);
        featuredArtists = (ListView)findViewById(R.id.featured_artists);
        favoriteButton = (Button)findViewById(R.id.favorite_button);
        assert stationDescription != null && featuredArtists != null && favoriteButton != null;

        setTitle(songzaStation.getName());
        stationDescription.setText(songzaStation.getDescription());
        featuredArtists.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, songzaStation.getFeaturedArtists()));
        favoriteButton.setOnClickListener(this);
    }

    @Override
    public Intent getParentActivityIntent() {
        Intent parentIntent = super.getParentActivityIntent();
        assert parentIntent != null;
        parentIntent.putExtra(SongbaseConstants.ACTIVITY, songzaActivity);
        return parentIntent;
    }

    @Override
    public void onClick(View v) {
        boolean isFavorite = songzaStation.toggleFavorite();
        favoriteButton.setText(isFavorite ? R.string.clear_favorite : R.string.save_favorite);

    }
}
