package com.example.android.popularmovies;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.data.MovieDbHelper;
import com.squareup.picasso.Picasso;

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

public class MainActivity extends AppCompatActivity {

    private ArrayAdapter<String> movieAdapter;
    private GridView moviesGrid;
    public String posterSize = "w185";
    public String baseUrl = "http://image.tmdb.org/t/p/";
    public ArrayList<String> moviePosterPaths;
    String movieJsonStr = null;
    String[] movieIds;

    private String releaseDate;
    private String movieOverview;
    private String averageVote;
    private String posterPath;
    private String movieTitle;


    private String MOVIE_LIST = "results";
    private String MOVIE_ID = "id";
    private String MOVIE_POSTER = "poster_path";
    private String RELEASE_DATE = "release_date";
    private String MOVIE_TITLE = "original_title";
    private String AVERAGE_VOTE = "average_vote";
    private String MOVIE_OVERVIEW = "overview";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        moviesGrid = (GridView) findViewById(R.id.gridview);
        //gridView.setAdapter(new ImageAdapter(this));
        if(moviePosterPaths == null) {
            moviePosterPaths = new ArrayList<>();
        }
        else if(!moviePosterPaths.isEmpty()) {
            moviePosterPaths.clear();
        }

        //FetchMovieInfo movieTask = new FetchMovieInfo();
        FetchMovieTask movieTask = new FetchMovieTask(this,movieAdapter);
        if(moviePosterPaths == null) {
            moviePosterPaths = new ArrayList<>();
        }
        movieTask.execute();

        moviesGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(MainActivity.this,"Opening Movie Info " + movieIds[position], Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                try {
                    JSONObject movieJson = new JSONObject(movieJsonStr);
                    JSONObject movieData = movieJson.getJSONArray("results").getJSONObject(position);
                    posterPath = baseUrl + posterSize + movieData.getString("poster_path");
                    releaseDate = "Release Date: " + movieData.getString("release_date");
                    movieOverview = movieData.getString("overview");
                    averageVote = "Average Vote: " + movieData.getString("vote_average");
                    movieTitle = movieData.getString("original_title");
                    //Log.d(this.getClass().getSimpleName(),"Parcel fields:\t"+posterPath+"\n\t"
                    //      +releaseDate+"\n\t"+movieOverview+"\n\t"+averageVote+"\n\t"+movieTitle);
                    MovieParcel mp = new MovieParcel(releaseDate, movieOverview,
                            averageVote, posterPath, movieTitle);
                    intent.putExtra("movie", mp);
                } catch (JSONException e) {
                    Log.e(this.getClass().getSimpleName(), "Error ", e);
                    return;
                }
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class MovieAdapter extends ArrayAdapter {
        private Context mContext;
        private ArrayList<String> mItems;

        public MovieAdapter(Context context, ArrayList<String> objects) {
            super(context, R.layout.grid_view_item,objects);
            this.mContext = context;
            this.mItems = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){

            //if the view is null than inflate it otherwise just fill the list with
            if(convertView == null){
                //inflate the layout
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                convertView = inflater.inflate(R.layout.grid_view_item, parent, false);
            }
            ImageView image =(ImageView) convertView.findViewById(R.id.grid_view_item);
            Picasso.with(mContext).load(mItems.get(position)).into(image);
            return  convertView;
        }

    }

    private void updateMovies () {
        //FetchMovieInfo movieTask = new FetchMovieInfo();
        FetchMovieTask movieTask = new FetchMovieTask(this,movieAdapter);
        movieTask.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }


/*
    public class FetchMovieInfo extends AsyncTask<String, Void, String[]> {

        //private ArrayAdapter<String> movieAdapter;
        //public String posterSize = "w185";
        //public String baseUrl = "http://image.tmdb.org/t/p/";
        //public ArrayList<String> moviePosterPaths;
        //String movieJsonStr = null;
        //String[] movieIds;

    private String releaseDate;
    private String movieOverview;
    private String averageVote;
    private String posterPath;
    private String movieTitle;


        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        //private ArrayAdapter<String> mMovieAdapter;
        //private final Context mContext;

        public FetchMovieInfo(Context context, ArrayAdapter<String> movieAdapter) {
            mContext = context;
            mMovieAdapter = movieAdapter;
        }

        private boolean DEBUG = true;

        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            Date date = new Date(time);
            SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
            return format.format(date).toString();
        }


        private String[] getMovieInfoFromJson(String movieJsonStr)
                throws JSONException {



            String[] resultStrs;
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(MOVIE_LIST);

            MovieDbHelper dbHelper = new MovieDbHelper(MainActivity.this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

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

                final String MOVIE_LIST = "results";
                final String MOVIE_ID = "id";
                final String MOVIE_POSTER = "poster_path";
                final String RELEASE_DATE = "release_date";
                final String MOVIE_TITLE = "original_title";
                final String AVERAGE_VOTE = "average_vote";


                    JSONObject movieData = movieArray.getJSONObject(i); //here we have everything in the json
                    movieId = movieData.getString(MOVIE_ID);
                    posterPath = movieData.getString(MOVIE_POSTER);
                    movieTitle = movieData.getString(MOVIE_TITLE);
                    averageVote = movieData.getString(AVERAGE_VOTE);
                    movieOverview = movieData.getString(MOVIE_OVERVIEW);
                    releaseDate = movieData.getString(RELEASE_DATE);

                    //resultStrs[i] = movieId; //attempting to separate values with _
                    moviePosterPaths.add(i, baseUrl + "/" + posterSize + "/" + posterPath);
                    movieIds[i] = movieId;

                    ContentValues movieValues = new ContentValues();

                    movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_KEY, movieId);
                    movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_URL, posterPath);
                    movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, movieTitle);
                    movieValues.put(MovieContract.MovieEntry.COLUMN_AVERAGE_VOTE, averageVote);
                    movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movieOverview);
                    movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);

                    resultStrs[i] = movieId;
                    //resultStrs[i] = movieId + "," + posterPath + "," + movieTitle + "," + averageVote
                      //      + "," + movieOverview + "," + releaseDate;

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

            SharedPreferences sharedPrefs =
                    PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            String sortBy = sharedPrefs.getString(
                    getString(R.string.sort_key),
                    getString(R.string.sort_popularity_key)
            );
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
                        .appendQueryParameter(SORT_PARAM, sortBy)
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

            if (moviePosterPaths == null) {
                moviePosterPaths = new ArrayList<>();
            }

            movieAdapter = new MovieAdapter(MainActivity.this, moviePosterPaths);
            moviesGrid.setAdapter(movieAdapter);
        }

    }*/


}
