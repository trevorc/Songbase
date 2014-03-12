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
    private final String mActivityId;
    private final String mName;
    private final long[] mStationIds;

    public Activity(JSONObject object)
            throws JSONException {
        mActivityId = object.getString("id");
        mName = object.getString("name");
        mStationIds = JsonUtil.getLongArray(object, "station_ids");
    }

    public String getActivityId() {
        return mActivityId;
    }

    public long[] getStationIds() {
        return mStationIds;
    }

    public ContentValues toContent() {
        final ContentValues values = new ContentValues();
        values.put(SongbaseContract.Activity.ACTIVITY_ID, mActivityId);
        values.put(SongbaseContract.Activity.NAME, mName);
        return values;
    }

}
