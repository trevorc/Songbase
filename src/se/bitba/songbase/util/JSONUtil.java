/*
 * Copyright (C) 2014 Bitbase LLC
 *
 * All rights reserved.
 */

package se.bitba.songbase.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class JSONUtil
{
    public static long[] getLongArray(JSONObject object, String name)
            throws JSONException {
        final JSONArray jsonArray = object.getJSONArray(name);
        final long[] longArray = new long[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); ++i) longArray[i] = jsonArray.getLong(i);
        return longArray;
    }
}
