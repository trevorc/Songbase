/*
 * Copyright (C) 2014 Bitbase LLC
 *
 * All rights reserved.
 */

package se.bitba.songbase.fetch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONArray;
import org.json.JSONException;
import se.bitba.songbase.SongbaseConstants;
import se.bitba.songbase.fetch.content.Activity;
import se.bitba.songbase.fetch.content.FeaturedArtist;
import se.bitba.songbase.fetch.content.Station;
import se.bitba.songbase.provider.SongbaseContract;
import se.bitba.songbase.util.ContextAsyncTask;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class FetchManager
{
    private static final String TAG = FetchManager.class.getSimpleName();
    private final Context mContext;
    private final AsyncHttpClient mHttpClient = new AsyncHttpClient();

    private static final Uri STATION_BARE_URI = SongbaseContract.Station.CONTENT_URI.buildUpon()
            .appendQueryParameter(SongbaseContract.Station.QUERY_PARAMETER_BARE, "true")
            .build();

    public FetchManager(Context context) {
        this.mContext = context;
    }

    private static ContentValues contentForBareStation(Activity activity, long stationId) {
        final ContentValues station = new ContentValues();
        station.put(SongbaseContract.StationColumns.ACTIVITY_ID, activity.getActivityId());
        station.put(SongbaseContract.StationColumns.STATION_ID, stationId);
        return station;
    }

    private static final class InsertActivitiesTask
            extends ContextAsyncTask
    {
        private final JSONArray mJsonArray;

        protected InsertActivitiesTask(Context context, JSONArray jsonArray) {
            super(context);
            mJsonArray = jsonArray;
        }

        @Override
        protected void doInBackground() {
            Log.d(TAG, String.format("got JSONArray[%d]", mJsonArray.length()));
            final ContentValues[] activities = new ContentValues[mJsonArray.length()];
            final List<ContentValues> stations = new ArrayList<>();

            try {
                for (int i = 0; i < mJsonArray.length(); ++i) {
                    final Activity activity = new Activity(mJsonArray.getJSONObject(i));
                    activities[i] = activity.toContent();
                    for (long stationId : activity.getStationIds()) {
                        stations.add(contentForBareStation(activity, stationId));
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "failed to parse activities", e);
                return;
            }

            final Context context = getContext();
            if (context == null) return;

            final ContentValues[] stationsArray = stations.toArray(
                    new ContentValues[stations.size()]);
            int numActivities = context.getContentResolver().bulkInsert(
                    SongbaseContract.Activity.CONTENT_URI, activities);
            int numStations = context.getContentResolver().bulkInsert(
                    STATION_BARE_URI, stationsArray);
            Log.d(TAG, String.format("inserted %d activities and %d stations",
                                     numActivities, numStations));
        }
    }

    public void fetchActivities() {
        Log.d(TAG, String.format("fetching %s", SongbaseConstants.ACTIVITIES_URL));
        mHttpClient.get(SongbaseConstants.ACTIVITIES_URL, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(final JSONArray response) {
                new InsertActivitiesTask(mContext, response).execute();
            }
        });
    }

    private interface StationIdQuery {
        String[] PROJECTION = {SongbaseContract.StationColumns.STATION_ID};
        String SELECTION = SongbaseContract.StationColumns.ACTIVITY_ID + "=?";
        int STATION_ID = 0;
    }

    private List<Long> fetchStationIds(final String activityId) {
        Log.d(TAG, String.format("fetchStationIds(%s)", activityId));
        Cursor cursor = mContext.getContentResolver().query(
                SongbaseContract.Station.CONTENT_URI,
                StationIdQuery.PROJECTION,
                StationIdQuery.SELECTION,
                new String[] {activityId},
                null);
        assert cursor != null;
        List<Long> stationIds = new LinkedList<>();
        try {
            while (cursor.moveToNext()) stationIds.add(cursor.getLong(StationIdQuery.STATION_ID));
            Log.d(TAG, String.format("found %s stations", cursor.getCount()));
        } finally {
            cursor.close();
        }
        return stationIds;
    }

    private static final class InsertStationsTask
            extends ContextAsyncTask
    {
        private final JSONArray mJsonArray;
        private String mActivityId;

        protected InsertStationsTask(Context context, JSONArray jsonArray, String activityId) {
            super(context);
            mJsonArray = jsonArray;
            mActivityId = activityId;
        }

        @Override
        protected void doInBackground() {
            Log.d(TAG, String.format("got JSONArray[%d]", mJsonArray.length()));
            final ContentValues[] stations = new ContentValues[mJsonArray.length()];
            final List<ContentValues> featuredArtists = new ArrayList<>();

            try {
                for (int i = 0; i < mJsonArray.length(); ++i) {
                    final Station station = new Station(mJsonArray.getJSONObject(i));
                    stations[i] = station.toContent(mActivityId);
                    for (FeaturedArtist artist : station.getFeaturedArtists()) {
                        featuredArtists.add(artist.toContent(station.getStationId()));
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "failed to insert stations", e);
                return;
            }

            final Context context = getContext();
            if (context == null) return;

            final ContentValues[] featuredArtistsArray = featuredArtists.toArray(
                    new ContentValues[featuredArtists.size()]);
            int numStations = context.getContentResolver().bulkInsert(
                    SongbaseContract.Station.CONTENT_URI, stations);
            int numFeaturedArtists = context.getContentResolver().bulkInsert(
                    SongbaseContract.FeaturedArtist.CONTENT_URI, featuredArtistsArray);
            Log.d(TAG, String.format("created %d stations and %d artists",
                                     numStations, numFeaturedArtists));
        }
    }

    public void fetchStations(final String activityId) {
        final RequestParams params = new RequestParams();
        for (long id : fetchStationIds(activityId)) params.add("id", Long.toString(id));

        Log.d(TAG, String.format("fetching %s with params %s", SongbaseConstants.STATIONS_URL, params));
        mHttpClient.get(SongbaseConstants.STATIONS_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONArray response) {
                new InsertStationsTask(mContext, response, activityId).execute();
            }
        });
    }
}
