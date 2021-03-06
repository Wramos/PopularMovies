package com.example.android.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.popularmovies.data.MovieContract.ReviewEntry;
import com.example.android.popularmovies.data.MovieContract.MovieEntry;
import com.example.android.popularmovies.data.MovieContract.TrailerEntry;
/**
 * Created by wramo on 11/7/2015.
 */
public class MovieDbHelper extends SQLiteOpenHelper {

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Manages a local database for movie data.
 */

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "movie.db";
    private final String LOG_TAG = this.getClass().getSimpleName();

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_TRAILER_TABLE = " CREATE TABLE " + TrailerEntry.TABLE_NAME + " (" +
                TrailerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                TrailerEntry.COLUMN_MOVIE_KEY + " TEXT NOT NULL, " +
                TrailerEntry.COLUMN_TRAILER_URL + " TEXT NOT NULL" +
                ");";

        final String SQL_CREATE_REVIEW_TABLE = " CREATE TABLE " + ReviewEntry.TABLE_NAME + " (" +
                ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                ReviewEntry.COLUMN_MOVIE_KEY + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_REVIEW + "TEXT NOT NULL " +
                ");";

        final String SQL_CREATE_MOVIE_TABLE = " CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                MovieEntry.COLUMN_REVIEW_KEY + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_TRAILER_KEY + " INTEGER NOT NULL, " +

                MovieEntry.COLUMN_AVERAGE_VOTE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_KEY + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_POSTER_URL + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, " +

                " FOREIGN KEY (" + MovieEntry.COLUMN_REVIEW_KEY + ") REFERENCES " +
                ReviewEntry.TABLE_NAME + " ( " + ReviewEntry._ID + "), " +

                " FOREIGN KEY (" + MovieEntry.COLUMN_TRAILER_KEY + ") REFERENCES " +
                TrailerEntry.TABLE_NAME + " ( " + TrailerEntry._ID + "), " +

                " UNIQUE (" + MovieEntry.COLUMN_MOVIE_KEY + ", " +
                ReviewEntry._ID + ", " + TrailerEntry._ID +
                ") ON CONFLICT REPLACE" +
                ");";

        Log.d(LOG_TAG, "Db Created");

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEW_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TRAILER_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrailerEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ReviewEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}