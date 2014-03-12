/*
 * Copyright (C) 2014 Bitbase LLC
 *
 * All rights reserved.
 */

package se.bitba.songbase.fetch.content;

import android.content.ContentValues;
import org.json.JSONException;
import org.json.JSONObject;
import se.bitba.songbase.provider.SongbaseContract;

import java.util.List;

public final class Station
{
    private final long mStationId;
    private final String mName;
    private final String mCoverUrl;
    private final String mDescription;
    private final List<FeaturedArtist> mFeaturedArtists;

    public Station(JSONObject object)
            throws JSONException
    {
        mStationId = object.getLong("id");
        mName = object.getString("name");
        mCoverUrl = object.getString("cover_url");
        mDescription = object.getString("description");
        mFeaturedArtists = FeaturedArtist.parseMany(object.getJSONArray("featured_artists"));
    }

    public List<FeaturedArtist> getFeaturedArtists() {
        return mFeaturedArtists;
    }

    public ContentValues toContent(String activityId) {
        final ContentValues values = new ContentValues();
        values.put(SongbaseContract.Station.STATION_ID, mStationId);
        values.put(SongbaseContract.Station.ACTIVITY_ID, activityId);
        values.put(SongbaseContract.Station.NAME, mName);
        values.put(SongbaseContract.Station.COVER_URL, mCoverUrl);
        values.put(SongbaseContract.Station.DESCRIPTION, mDescription);
        return values;
    }

    public long getStationId() {
        return mStationId;
    }
}
