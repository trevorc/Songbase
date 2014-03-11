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
import se.bitba.songbase.util.JsonUtil;

public final class Activity
{
    private final String activityId;
    private final String name;
    private final long[] stationIds;

    public Activity(JSONObject object)
            throws JSONException {
        activityId = object.getString("id");
        name = object.getString("name");
        stationIds = JsonUtil.getLongArray(object, "station_ids");
    }

    public String getActivityId() {
        return activityId;
    }

    public long[] getStationIds() {
        return stationIds;
    }

    public ContentValues toContent() {
        final ContentValues values = new ContentValues();
        values.put(SongbaseContract.Activity.ACTIVITY_ID, activityId);
        values.put(SongbaseContract.Activity.NAME, name);
        return values;
    }

}
