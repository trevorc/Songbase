/*
 * Copyright (C) 2014 Bitbase LLC
 *
 * All rights reserved.
 */

package se.bitba.songbase.model;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONException;
import org.json.JSONObject;
import se.bitba.songbase.util.JSONUtil;

public class SongzaActivity
        implements Parcelable
{
    private final String id;
    private final String name;
    private final long[] stations;

    public SongzaActivity(String id, String name, long[] stations) {
        this.id = id;
        this.name = name;
        this.stations = stations;
    }

    public String getName() {
        return name;
    }

    public long[] getStations() { return stations; }

    /* ModelBuilder implementation */

    public static final ModelBuilder<SongzaActivity> BUILDER
            = new ModelBuilder<SongzaActivity>() {
        @Override
        public SongzaActivity fromJSON(JSONObject object) throws JSONException {
            return new SongzaActivity(object.getString("id"),
                                      object.getString("name"),
                                      JSONUtil.getLongArray(object, "station_ids"));
        }
    };

    /* Parcelable implementation */

    private SongzaActivity(Parcel in) {
        this(in.readString(), in.readString(), in.createLongArray());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(name);
        out.writeLongArray(stations);
    }

    public static final Parcelable.Creator<SongzaActivity> CREATOR
            = new Parcelable.Creator<SongzaActivity>() {
        @Override
        public SongzaActivity createFromParcel(Parcel in) {
            return new SongzaActivity(in);
        }

        @Override
        public SongzaActivity[] newArray(int size) {
            return new SongzaActivity[size];
        }
    };
}
