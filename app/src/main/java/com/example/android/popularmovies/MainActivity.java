package com.example.android.popularmovies;

import android.app.Activity;
import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

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
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayAdapter<String> movieAdapter;
    private GridView moviesGrid;
    public String posterSize = "w185";
    public String baseUrl = "http://image.tmdb.org/t/p/";
    public ArrayList<String> moviePosterPaths;
    String movieJsonStr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        moviesGrid = (GridView) findViewById(R.id.gridview);
        //gridView.setAdapter(new ImageAdapter(this));

        FetchMovieInfo movieTask = new FetchMovieInfo();
        movieTask.execute();

        /*if(movieJsonStr != null) {
            moviePosterPaths =
        }*/

        moviesGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this,"Opening Movie Info " + position, Toast.LENGTH_SHORT).show();
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

    public class FetchMovieInfo extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchMovieInfo.class.getSimpleName();

        //private String[][] getMovieInfoFromJson(String movieJsonStr, int numMovies)
        private String[] getMovieInfoFromJson(String movieJsonStr)
                throws JSONException {

            final String MOVIE_LIST = "results";
            final String MOVIE_ID = "id";
            final String MOVIE_POSTER = "poster_path";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(MOVIE_LIST);

            //String[][] resultStrs = new String[numMovies][2]; //two dimensional array, storing movie ids and poster paths
            String[]resultStrs = new String[movieArray.length()]; //two dimensional array, storing movie ids and poster paths

            for(int i = 0; i < movieArray.length(); i++) {
                String movieId;
                String posterPath;

                JSONObject movieData = movieArray.getJSONObject(i);
                movieId = movieData.getString(MOVIE_ID);
                posterPath = movieData.getString(MOVIE_POSTER);

                //resultStrs[i] = movieId + "_" + posterPath; //attempting to separate values with _
                resultStrs[i] = movieId; //attempting to separate values with _
                moviePosterPaths.add(i,baseUrl+posterPath);
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

            //int numMovies = 12;
            String sortBy = "popularity.desc";

            //get prefs here

            try {

                final String MOVIE_BASE_URL =
                        "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_PARAM = "sort_by";
                final String API_KEY =
                        "api_key";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM,sortBy)
                        .appendQueryParameter(API_KEY,"") //add your own api key
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

            if(movieJsonStr != null) {

            }
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
