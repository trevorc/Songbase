/*
 * Copyright (C) 2014 Bitbase LLC
 *
 * All rights reserved.
 */

package se.bitba.songbase.model;

import org.json.JSONException;
import org.json.JSONObject;

public interface ModelBuilder<Model>
{
    public Model fromJSON(JSONObject object) throws JSONException;
}
