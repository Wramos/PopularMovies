package com.example.android.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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
        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }

    }

    public static class DetailFragment extends Fragment {
        private static final String LOG_TAG = DetailFragment.class.getSimpleName();

        private TextView titleTextView;
        private TextView overviewTextView;
        private TextView voteTextView;
        private TextView releaseDateTextView;


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail,container,false);

            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra("movie")) {
                MovieParcel mp = intent.getParcelableExtra("movie");

                titleTextView = (TextView) rootView.findViewById(R.id.original_title);
                overviewTextView = (TextView) rootView.findViewById(R.id.overview);
                voteTextView = (TextView) rootView.findViewById(R.id.vote_average);
                releaseDateTextView = (TextView) rootView.findViewById(R.id.release_date);

                Log.d(LOG_TAG, "Parcel fields:\t" + mp.movieTitle + "\n\t"
                        + mp.posterPath + "\n\t" + mp.averageVote + "\n\t" + mp.movieOverview + "\n\t" + mp.releaseDate);

                titleTextView.setText(mp.movieTitle);
                overviewTextView.setText(mp.movieOverview);
                voteTextView.setText(mp.averageVote);
                releaseDateTextView.setText(mp.releaseDate);

                ImageView image = (ImageView) rootView.findViewById(R.id.thumbnail);
                Picasso.with(this.getContext()).load(mp.posterPath).into(image);
            }
            return rootView;
        }

    }

}
