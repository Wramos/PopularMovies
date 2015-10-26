package com.example.android.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Wilbert on 10/25/2015.
 */
public class MovieParcel  implements Parcelable {
    //private int mData;
    String releaseDate;
    String movieOverview;
    String averageVote;
    String posterPath;
    String movieTitle;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.releaseDate);
        dest.writeString(this.movieOverview);
        dest.writeString(this.averageVote);
        dest.writeString(this.posterPath);
        dest.writeString(this.movieTitle);
    }

    public MovieParcel(String releaseDate, String movieOverview, String averageVote,
                       String posterPath, String movieTitle) {
        this.releaseDate = releaseDate;
        this.movieOverview = movieOverview;
        this.averageVote = averageVote;
        this.posterPath = posterPath;
        this.movieTitle = movieTitle;
    }

    protected MovieParcel(Parcel in) {
        this.releaseDate = in.readString();
        this.movieOverview = in.readString();
        this.averageVote = in.readString();
        this.posterPath = in.readString();
        this.movieTitle = in.readString();
    }

    public static final Creator<MovieParcel> CREATOR = new Creator<MovieParcel>() {
        public MovieParcel createFromParcel(Parcel source) {
            return new MovieParcel(source);
        }

        public MovieParcel[] newArray(int size) {
            return new MovieParcel[size];
        }
    };
}
