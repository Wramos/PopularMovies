package com.example.android.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

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

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private ArrayAdapter<String> movieAdapter;

    private String posterSize = "w185";
    private String baseUrl = "http://image.tmdb.org/t/p/";

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /*movieAdapter =
                new ArrayAdapter<String>(
                        getActivity(),
                        R.layout.,
                        R.id.,
                        new ArrayList<String>()
                );*/

        return inflater.inflate(R.layout.fragment_main, container, false);
    }


    private void updateMovies() {
        FetchMovieInfo movieTask = new FetchMovieInfo();
        movieTask.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    public class FetchMovieInfo extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchMovieInfo.class.getSimpleName();

        //private String[][] getMovieInfoFromJson(String movieJsonStr, int numMovies)
        private String[] getMovieInfoFromJson(String movieJsonStr, int numMovies)
            throws JSONException {

            final String MOVIE_LIST = "results";
            final String MOVIE_ID = "id";
            final String MOVIE_POSTER = "poster_path";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(MOVIE_LIST);

            //String[][] resultStrs = new String[numMovies][2]; //two dimensional array, storing movie ids and poster paths
            String[]resultStrs = new String[numMovies]; //two dimensional array, storing movie ids and poster paths

            for(int i = 0; i < movieArray.length(); i++) {
                String movieId;
                String posterPath;

                JSONObject movieData = movieArray.getJSONObject(i);
                movieId = movieData.getString(MOVIE_ID);
                posterPath = movieData.getString(MOVIE_POSTER);

                resultStrs[i] = movieId + "_" + posterPath; //attempting to separate values with _
                /*resultStrs[i][0] = movieId;
                resultStrs[i][1] = posterPath;*/
            }
            return resultStrs;
        }


        @Override
        protected String[] doInBackground(String... params) {
            /*if(params.length == 0) {
                return null;
            }*/

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String movieJsonStr = null;
            int numMovies = 12;
            String sortBy = "popularity.desc";

            //get prefs here

            try {

                final String MOVIE_BASE_URL =
                        "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_PARAM = "sort_by";
                final String API_KEY =
                        ""; //fill your own in

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM,sortBy)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if(inputStream == null)
                {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line = reader.readLine()) != null) {
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
                return getMovieInfoFromJson(movieJsonStr,numMovies); //can only return one dimensional array
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onPostExecute(String[] result) {

        }

    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mThumbIds.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }


            //imageView.setImageResource(mThumbIds[position]);
            return imageView;
        }

        // references to our images
        private Integer[] mThumbIds = {
                /*R.drawable.sample_2, R.drawable.sample_3,
                R.drawable.sample_4, R.drawable.sample_5,
                R.drawable.sample_6, R.drawable.sample_7,
                R.drawable.sample_0, R.drawable.sample_1,
                R.drawable.sample_2, R.drawable.sample_3,
                R.drawable.sample_4, R.drawable.sample_5,
                R.drawable.sample_6, R.drawable.sample_7,
                R.drawable.sample_0, R.drawable.sample_1,
                R.drawable.sample_2, R.drawable.sample_3,
                R.drawable.sample_4, R.drawable.sample_5,
                R.drawable.sample_6, R.drawable.sample_7*/
        };
    }

}
