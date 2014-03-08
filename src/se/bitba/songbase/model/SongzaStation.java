/*
 * Copyright (C) 2014 Bitbase LLC
 *
 * All rights reserved.
 */

package se.bitba.songbase.model;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static se.bitba.songbase.util.URLUtil.makeURL;

public class SongzaStation
        implements Parcelable {
    private final long id;
    private final String name;
    private final URL coverURL;
    private final String description;
    private final List<String> featuredArtists;

    public SongzaStation(long id, String name, URL coverURL, String description, List<String> featuredArtists) {
        this.id = id;
        this.name = name;
        this.coverURL = coverURL;
        this.description = description;
        this.featuredArtists = featuredArtists;
    }

    private SongzaStation(Parcel in) {
        this(in.readLong(), in.readString(), makeURL(in.readString()),
             in.readString(), in.createStringArrayList());
    }

    private static List<String> parseFeaturedArtists(JSONArray featuredArtists)
            throws JSONException {
        final List<String> artists = new ArrayList<>();
        for (int i = 0; i < featuredArtists.length(); ++i) {
            artists.add(featuredArtists.getJSONObject(i).getString("name"));
        }
        return artists;
    }

    /* ModelBuilder implementation */

    public static final ModelBuilder<SongzaStation> BUILDER
            = new ModelBuilder<SongzaStation>() {
        @Override
        public SongzaStation fromJSON(JSONObject object) throws JSONException {
            return new SongzaStation(object.getLong("id"),
                                     object.getString("name"),
                                     makeURL(object.getString("cover_url")),
                                     object.getString("description"),
                                     parseFeaturedArtists(object.getJSONArray("featured_artists")));
        }
    };

    /* Parcelable implementation */

    public static final Parcelable.Creator<SongzaStation> CREATOR
            = new Creator<SongzaStation>() {
        @Override
        public SongzaStation createFromParcel(Parcel in) {
            return new SongzaStation(in);
        }

        @Override
        public SongzaStation[] newArray(int size) {
            return new SongzaStation[size];
        }
    };

    public String getName() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(id);
        out.writeString(name);
        out.writeString(coverURL.toString());
        out.writeString(description);
        out.writeStringList(featuredArtists);
    }
}
