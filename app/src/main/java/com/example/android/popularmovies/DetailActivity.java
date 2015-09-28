package com.example.android.popularmovies;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {

    String movieJsonStr = null;
    public ArrayList<String> movieInfo;
    private ArrayAdapter<String> movieAdapter;
    private static String movieId;
    public String posterSize = "w185";
    public String baseUrl = "http://image.tmdb.org/t/p/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        /*if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }*/
        FetchMovieInfo movieTask = new FetchMovieInfo();
        if(movieInfo == null) {
            movieInfo = new ArrayList<>();
        }
        movieTask.execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class DetailFragment extends Fragment {
        private static final String LOG_TAG = DetailFragment.class.getSimpleName();


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail,container,false);

            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                movieId = intent.getStringExtra(Intent.EXTRA_TEXT);

                //get info from server then inject into views
                //((TextView) rootView.findViewById());
            }
            return rootView;
        }

    }

    public class FetchMovieInfo extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchMovieInfo.class.getSimpleName();

        //private String[][] getMovieInfoFromJson(String movieJsonStr, int numMovies)
        private String[] getMovieInfoFromJson(String movieJsonStr)
                throws JSONException {

            final String MOVIE_LIST = "movie_results";
            final String MOVIE_POSTER = "backdrop_path";
            final String VOTE = "vote_average";
            final String DATE = "release-date";
            final String ORIGINAL_TITLE = "original_title";
            final String OVERVIEW = "overview";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(MOVIE_LIST);

            if (movieInfo == null) {
                movieInfo = new ArrayList<>();
            }

            String[] resultStrs = new String[movieArray.length()];

            String posterPath;
            String title;
            String overview;
            String averageVote;
            String releaseDate;

            JSONObject movieData = movieArray.getJSONObject(0);
            title = movieData.getString(ORIGINAL_TITLE);
            posterPath = movieData.getString(MOVIE_POSTER);
            overview = movieData.getString(OVERVIEW);
            averageVote = movieData.getString(VOTE);
            releaseDate = movieData.getString(DATE);

            //resultStrs[i] = movieId + "_" + posterPath; //attempting to separate values with _
            //resultStrs[i] = movieId; //attempting to separate values with _
            movieInfo.add(0, title);
            movieInfo.add(1, baseUrl + "/" + posterSize + "/" + posterPath);
            movieInfo.add(2, overview);
            movieInfo.add(3, averageVote);
            movieInfo.add(4, releaseDate);
            //movieIds[i] = movieId;
            /*resultStrs[i][0] = movieId;
            resultStrs[i][1] = posterPath;*/

            return resultStrs;
        }


        @Override
        protected String[] doInBackground(String... params) {
            if(params.length == 0) {
                return null;
            }
            else if (movieId == null) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {

                final String MOVIE_BASE_URL =
                        "http://api.themoviedb.org/3/find/"+movieId;
                final String EXTERNAL_SOURCE = "external_source";
                final String API_KEY =
                        "api_key";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(EXTERNAL_SOURCE, "imdb_id")
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
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                movieJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
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
                //return getMovieInfoFromJson(movieJsonStr,numMovies); //can only return one dimensional array
                return getMovieInfoFromJson(movieJsonStr); //can only return one dimensional array
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onPostExecute(String[] result) {

            /*if(movieJsonStr != null) {

            }
            if (movieInfo == null) {
                movieInfo = new ArrayList<>();
            }*/

            movieAdapter = new MovieAdapter(DetailActivity.this, movieInfo);
            //moviesGrid.setAdapter(movieAdapter);
        }

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
            /*((TextView)convertView.findViewById(R.id.original_title)).setText(mItems.get(0));
            ImageView image =(ImageView) convertView.findViewById(R.id.thumbnail);
            Picasso.with(mContext).load(mItems.get(1)).into(image);
            ((TextView)convertView.findViewById(R.id.overview)).setText(mItems.get(2));
            ((TextView)convertView.findViewById(R.id.vote_average)).setText(mItems.get(3));
            ((TextView)convertView.findViewById(R.id.release_date)).setText(mItems.get(4));*/
            return  convertView;
            /*
            movieInfo.add(0, title);
            movieInfo.add(1, baseUrl + "/" + posterSize + "/" + posterPath);
            movieInfo.add(2, overview);
            movieInfo.add(3, averageVote);
            movieInfo.add(4, releaseDate);
             */
        }

    }

}
