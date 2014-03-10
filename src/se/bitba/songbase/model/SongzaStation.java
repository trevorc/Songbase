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

import java.util.ArrayList;
import java.util.List;

public class SongzaStation
        implements Parcelable {
    private final long id;
    private final String name;
    private final String coverURL;
    private final String description;
    private final List<String> featuredArtists;
    private boolean favorite;

    public SongzaStation(long id, String name, String coverURL, String description,
                         List<String> featuredArtists, boolean favorite) {
        this.id = id;
        this.name = name;
        this.coverURL = coverURL;
        this.description = description;
        this.featuredArtists = featuredArtists;
        this.favorite = favorite;
    }

    private SongzaStation(Parcel in) {
        this(in.readLong(), in.readString(), in.readString(),
             in.readString(), in.createStringArrayList(),
             in.readByte() == 1);
    }

    private static List<String> parseFeaturedArtists(JSONArray featuredArtists)
            throws JSONException {
        final List<String> artists = new ArrayList<>();
        for (int i = 0; i < featuredArtists.length(); ++i) {
            artists.add(featuredArtists.getJSONObject(i).getString("name"));
        }
        return artists;
    }

    /* Accessors */

    public String getCoverURL() {
        return coverURL;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getFeaturedArtists() {
        return featuredArtists;
    }

    /* ModelBuilder implementation */

    public static final ModelBuilder<SongzaStation> BUILDER
            = new ModelBuilder<SongzaStation>() {
        @Override
        public SongzaStation fromJSON(JSONObject object) throws JSONException {
            return new SongzaStation(object.getLong("id"),
                                     object.getString("name"),
                                     object.getString("cover_url"),
                                     object.getString("description"),
                                     parseFeaturedArtists(object.getJSONArray("featured_artists")),
                                     false);
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

    public boolean toggleFavorite() {
        return favorite = !favorite;
    }

    public boolean isFavorite() {
        return favorite;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(id);
        out.writeString(name);
        out.writeString(coverURL);
        out.writeString(description);
        out.writeStringList(featuredArtists);
        out.writeByte((byte)(favorite ? 1 : 0));
    }

}
