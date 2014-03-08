/*
 * Copyright (C) 2014 Bitbase LLC
 *
 * All rights reserved.
 */

package se.bitba.songbase.util;

import android.util.Log;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONArray;
import org.json.JSONException;
import se.bitba.songbase.SongbaseConstants;
import se.bitba.songbase.model.ModelBuilder;
import se.bitba.songbase.model.SongzaActivity;
import se.bitba.songbase.model.SongzaStation;

import java.util.ArrayList;
import java.util.List;

public class SongzaAPIClient
{
    private static final String TAG = SongzaAPIClient.class.getCanonicalName();
    private final AsyncHttpClient httpClient = new AsyncHttpClient();

    protected <Model> void getList(String url, ModelBuilder<Model> builder, FetchObserver<List<Model>> observer) {
        getList(url, new RequestParams(), builder, observer);
    }

    protected <Model> void getList(final String url,
                                   final RequestParams params,
                                   final ModelBuilder<Model> builder,
                                   final FetchObserver<List<Model>> observer) {
        httpClient.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONArray response) {
                List<Model> modelObjects = new ArrayList<>();
                try {
                    for (int i = 0; i < response.length(); ++i) {
                        modelObjects.add(builder.fromJSON(response.getJSONObject(i)));
                    }
                } catch (JSONException e) {
                    onFailure(e, response);
                }
                observer.onSuccess(modelObjects);
            }

            @Override
            public void onFailure(Throwable e, JSONArray errorResponse) {
                super.onFailure(e, errorResponse);
                Log.e(TAG, "request failed", e);
                observer.onFailure();
            }
        });
    }

    public void fetchActivities(final FetchObserver<List<SongzaActivity>> observer) {
        getList(SongbaseConstants.ACTIVITIES_URL.toString(), SongzaActivity.BUILDER, observer);
    }

    public void fetchStations(long[] stationIds, final FetchObserver<List<SongzaStation>> observer) {
        final RequestParams params = new RequestParams();
        for (long id : stationIds) params.add("id", Long.toString(id));
        getList(SongbaseConstants.STATIONS_URL.toString(), params, SongzaStation.BUILDER, observer);
    }
}
