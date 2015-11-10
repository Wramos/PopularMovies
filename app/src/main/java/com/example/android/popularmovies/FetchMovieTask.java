package com.example.android.popularmovies;

/**
 * Created by wramo on 11/8/2015.
 */
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

        import android.content.ContentValues;
        import android.content.Context;
        import android.content.SharedPreferences;
        import android.net.Uri;
        import android.os.AsyncTask;
        import android.preference.PreferenceManager;
        import android.text.format.Time;
        import android.util.Log;
        import android.widget.ArrayAdapter;
        import android.widget.GridView;

        import com.example.android.popularmovies.data.MovieContract.MovieEntry;
        //import com.example.android.sunshine.app.data.WeatherContract.WeatherEntry;

        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.net.HttpURLConnection;
        import java.net.URL;
        import java.text.SimpleDateFormat;
        import java.util.ArrayList;
        import java.util.Date;
        import java.util.Vector;

public class FetchMovieTask extends AsyncTask<String, Void, String[]> {

    private ArrayAdapter<String> movieAdapter;
    private GridView moviesGrid;
    public String posterSize = "w185";
    public String baseUrl = "http://image.tmdb.org/t/p/";
    public ArrayList<String> moviePosterPaths;
    String movieJsonStr = null;
    String[] movieIds;

    /*private String releaseDate;
    private String movieOverview;
    private String averageVote;
    private String posterPath;
    private String movieTitle;*/

    final String MOVIE_LIST = "results";
    final String MOVIE_ID = "id";
    final String MOVIE_POSTER = "poster_path";
    final String RELEASE_DATE = "release_date";
    final String MOVIE_TITLE = "original_title";
    final String AVERAGE_VOTE = "average_vote";
    final String MOVIE_OVERVIEW = "overview";

    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

    private ArrayAdapter<String> mMovieAdapter;
    private final Context mContext;

    public FetchMovieTask(Context context, ArrayAdapter<String> movieAdapter) {
        mContext = context;
        mMovieAdapter = movieAdapter;
    }

    private boolean DEBUG = true;

    /* The date/time conversion code is going to be moved outside the asynctask later,
     * so for convenience we're breaking it out into its own method now.
     */
    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date).toString();
    }


    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param locationSetting The location string used to request updates from the server.
     * @param cityName A human-readable city name, e.g "Mountain View"
     * @param lat the latitude of the city
     * @param lon the longitude of the city
     * @return the row ID of the added location.
     */

    /*
        Students: This code will allow the FetchWeatherTask to continue to return the strings that
        the UX expects so that we can continue to test the application even once we begin using
        the database.
     */


    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */

    private String[] getMovieInfoFromJson(String movieJsonStr)
            throws JSONException {



        String[] resultStrs;
        JSONObject movieJson = new JSONObject(movieJsonStr);
        JSONArray movieArray = movieJson.getJSONArray(MOVIE_LIST);
        if(!moviePosterPaths.isEmpty()) {
            moviePosterPaths.clear();
        }
        else if (moviePosterPaths == null) {
            moviePosterPaths = new ArrayList<>();
        }

        resultStrs = new String[movieArray.length()]; //two dimensional array, storing movie ids and poster paths

        if (movieIds == null) {
            movieIds = new String[movieArray.length()];
        }

        try {

            Vector<ContentValues> cVVector = new Vector<ContentValues>(movieArray.length());

            String movieId;
            String posterPath;
            String releaseDate;
            String movieOverview;
            String averageVote;
            String movieTitle;
            resultStrs = new String[movieArray.length()];
            for (int i = 0; i < movieArray.length(); i++) {

                /*final String MOVIE_LIST = "results";
                final String MOVIE_ID = "id";
                final String MOVIE_POSTER = "poster_path";
                final String RELEASE_DATE = "release_date";
                final String MOVIE_TITLE = "original_title";
                final String AVERAGE_VOTE = "average_vote";*/


                JSONObject movieData = movieArray.getJSONObject(i); //here we have everything in the json
                movieId = movieData.getString(MOVIE_ID);
                posterPath = movieData.getString(MOVIE_POSTER);
                movieTitle = movieData.getString(MOVIE_TITLE);
                averageVote = movieData.getString(AVERAGE_VOTE);
                movieOverview = movieData.getString(MOVIE_OVERVIEW);
                releaseDate = movieData.getString(RELEASE_DATE);

                //resultStrs[i] = movieId; //attempting to separate values with _
                //moviePosterPaths.add(i, baseUrl + "/" + posterSize + "/" + posterPath);
                //movieIds[i] = movieId;

                ContentValues movieValues = new ContentValues();

                movieValues.put(MovieEntry.COLUMN_MOVIE_KEY, movieId);
                movieValues.put(MovieEntry.COLUMN_POSTER_URL, posterPath);
                movieValues.put(MovieEntry.COLUMN_MOVIE_TITLE, movieTitle);
                movieValues.put(MovieEntry.COLUMN_AVERAGE_VOTE, averageVote);
                movieValues.put(MovieEntry.COLUMN_OVERVIEW, movieOverview);
                movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, releaseDate);

                resultStrs[i] = movieId + "," + posterPath + "," + movieTitle + "," + averageVote
                        + "," + movieOverview + "," + releaseDate;

                cVVector.add(movieValues);
            }

            if ( cVVector.size() > 0) {
                //bulk insert here to add to db
            }

            Log.d(LOG_TAG, "FetchMovieTask Complete. " + cVVector.size() + " Inserted");

            return resultStrs;

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected String[] doInBackground(String... params) {

        // If there's no zip code, there's nothing to look up.  Verify size of params.
        if (params.length == 0) {
            return null;
        }
        String locationQuery = params[0];

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        /*SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String sortBy = sharedPrefs.getString(
                getString(R.string.sort_key),
                getString(R.string.sort_popularity_key)
        );*/
        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            final String MOVIE_BASE_URL =
                    "http://api.themoviedb.org/3/discover/movie?";
            final String SORT_PARAM = "sort_by";
            final String API_KEY =
                    "api_key";

            Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    //.appendQueryParameter(SORT_PARAM, sortBy)
                    .appendQueryParameter(API_KEY, "") //add your own api key
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            movieJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            return getMovieInfoFromJson(movieJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        // This will only happen if there was an error getting or parsing the forecast.
        return null;
    }

    @Override
    protected void onPostExecute(String[] result) {
        if ( result != null && mMovieAdapter != null) {
            mMovieAdapter.clear();
            for(String movieForecastStr : result) {
                mMovieAdapter.add(movieForecastStr);
            }
        }

        if (moviePosterPaths == null) {
            moviePosterPaths = new ArrayList<>();
        }

        //movieAdapter = new MovieAdapter(mContext, moviePosterPaths);
        //moviesGrid.setAdapter(movieAdapter);
    }
}